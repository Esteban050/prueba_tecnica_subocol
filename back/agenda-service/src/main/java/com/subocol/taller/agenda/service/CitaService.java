package com.subocol.taller.agenda.service;

import com.subocol.taller.agenda.domain.*;
import com.subocol.taller.agenda.dto.AgendarCitaRequest;
import com.subocol.taller.agenda.dto.CitaResponse;
import com.subocol.taller.agenda.event.CitaAgendadaEvent;
import com.subocol.taller.agenda.event.CitaCanceladaEvent;
import com.subocol.taller.agenda.exception.CupoAgotadoException;
import com.subocol.taller.agenda.exception.OrdenCerradaNoPermiteCitaException;
import com.subocol.taller.agenda.exception.RecursoNoEncontradoException;
import com.subocol.taller.agenda.repository.CitaRepository;
import com.subocol.taller.agenda.repository.FranjaCupoRepository;
import com.subocol.taller.agenda.repository.OperarioRepository;
import com.subocol.taller.agenda.repository.OrdenReplicaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CitaService {

    private static final Logger log = LoggerFactory.getLogger(CitaService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CitaRepository citaRepository;
    private final FranjaCupoRepository franjaCupoRepository;
    private final OperarioRepository operarioRepository;
    private final OrdenReplicaRepository ordenReplicaRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:taller.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key-cita-agendada:cita.agendada}")
    private String routingKeyCitaAgendada;

    @Value("${app.rabbitmq.routing-key-cita-cancelada:cita.cancelada}")
    private String routingKeyCitaCancelada;

    public CitaService(CitaRepository citaRepository,
                       FranjaCupoRepository franjaCupoRepository,
                       OperarioRepository operarioRepository,
                       OrdenReplicaRepository ordenReplicaRepository,
                       RabbitTemplate rabbitTemplate) {
        this.citaRepository = citaRepository;
        this.franjaCupoRepository = franjaCupoRepository;
        this.operarioRepository = operarioRepository;
        this.ordenReplicaRepository = ordenReplicaRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public CitaResponse agendarCita(AgendarCitaRequest req) {
        // RF-17: Validar que la orden no esté cerrada en la réplica local
        Optional<OrdenReplica> replicaOpt = ordenReplicaRepository.findById(req.getOrdenId());
        if (replicaOpt.isPresent() && replicaOpt.get().estaCerrada()) {
            throw new OrdenCerradaNoPermiteCitaException(
                    "No se permite agendar citas sobre una orden cerrada (Orden #" + req.getOrdenId() + ")"
            );
        }

        Operario operario = operarioRepository.findById(req.getOperarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Operario no encontrado con id: " + req.getOperarioId()));

        FranjaCupo franja = franjaCupoRepository.findById(req.getFranjaCupoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Franja no encontrada con id: " + req.getFranjaCupoId()));

        if (!franja.getOperarioId().equals(operario.getId())) {
            throw new IllegalArgumentException("La franja indicada no corresponde al operario seleccionado");
        }

        // RF-15: Operación atómica en BD para reservar cupo
        int filasAfectadas = franjaCupoRepository.incrementarCupoAtomico(franja.getId());
        if (filasAfectadas == 0) {
            throw new CupoAgotadoException(String.format(
                    "Cupo agotado: La franja de %s a %s para el operario %s ya alcanzó su cupo máximo (%d/%d)",
                    franja.getInicio().format(FORMATTER),
                    franja.getFin().format(FORMATTER),
                    operario.getNombre(),
                    franja.getCupoMaximo(),
                    franja.getCupoMaximo()
            ));
        }

        Cita cita = new Cita(
                req.getOrdenId(),
                operario.getId(),
                franja.getId(),
                req.getTipo()
        );
        Cita guardada = citaRepository.save(cita);

        // Obtener teléfono del cliente desde la réplica
        String telefono = replicaOpt.map(OrdenReplica::getClienteTelefono).orElse("");
        String clienteNombre = replicaOpt.map(OrdenReplica::getClienteNombre).orElse("Cliente");
        String franjaTexto = franja.getInicio().format(FORMATTER) + " - " + franja.getFin().format(FORMATTER);

        // Publicar evento CitaAgendada (RF-20)
        CitaAgendadaEvent evento = new CitaAgendadaEvent(
                UUID.randomUUID().toString(),
                guardada.getId(),
                guardada.getOrdenId(),
                telefono,
                clienteNombre,
                franjaTexto,
                operario.getNombre(),
                guardada.getTipo().name(),
                LocalDateTime.now()
        );
        publicarEvento(routingKeyCitaAgendada, evento);

        return CitaResponse.from(guardada, operario.getNombre(), franjaTexto);
    }

    public CitaResponse cancelarCita(Long citaId, String motivo) {
        Cita cita = citaRepository.findById(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cita no encontrada con id: " + citaId));

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new IllegalStateException("La cita ya se encuentra cancelada");
        }

        // RF-16: Liberar cupo de forma atómica en la misma transacción
        franjaCupoRepository.decrementarCupoAtomico(cita.getFranjaCupoId());

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setMotivoCancelacion(motivo != null ? motivo : "CANCELADA_POR_OPERARIO");
        cita.setFechaCancelacion(LocalDateTime.now());
        Cita actualizada = citaRepository.save(cita);

        FranjaCupo franja = franjaCupoRepository.findById(cita.getFranjaCupoId()).orElse(null);
        Operario operario = operarioRepository.findById(cita.getOperarioId()).orElse(null);
        Optional<OrdenReplica> replicaOpt = ordenReplicaRepository.findById(cita.getOrdenId());

        String telefono = replicaOpt.map(OrdenReplica::getClienteTelefono).orElse("");
        String franjaTexto = franja != null ? franja.getInicio().format(FORMATTER) + " - " + franja.getFin().format(FORMATTER) : "";
        String operarioNombre = operario != null ? operario.getNombre() : "Operario #" + cita.getOperarioId();

        // Publicar evento CitaCancelada con motivo (RF-20)
        CitaCanceladaEvent evento = new CitaCanceladaEvent(
                UUID.randomUUID().toString(),
                actualizada.getId(),
                actualizada.getOrdenId(),
                telefono,
                franjaTexto,
                actualizada.getMotivoCancelacion(),
                LocalDateTime.now()
        );
        publicarEvento(routingKeyCitaCancelada, evento);

        return CitaResponse.from(actualizada, operarioNombre, franjaTexto);
    }

    @Transactional(readOnly = true)
    public List<CitaResponse> listarCitas(Long ordenId, Long operarioId, Long franjaId) {
        List<Cita> citas;
        if (ordenId != null) {
            citas = citaRepository.findByOrdenId(ordenId);
        } else if (operarioId != null) {
            citas = citaRepository.findByOperarioId(operarioId);
        } else if (franjaId != null) {
            citas = citaRepository.findByFranjaCupoId(franjaId);
        } else {
            citas = citaRepository.findAllByOrderByFechaCreacionDesc();
        }

        return citas.stream().map(c -> {
            String opNombre = operarioRepository.findById(c.getOperarioId())
                    .map(Operario::getNombre)
                    .orElse("Operario #" + c.getOperarioId());
            String franjaTexto = franjaCupoRepository.findById(c.getFranjaCupoId())
                    .map(f -> f.getInicio().format(FORMATTER) + " - " + f.getFin().format(FORMATTER))
                    .orElse("Franja #" + c.getFranjaCupoId());
            return CitaResponse.from(c, opNombre, franjaTexto);
        }).collect(Collectors.toList());
    }

    private void publicarEvento(String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, evento);
            log.info("Evento publicado con éxito en Agenda [routingKey: {}]: {}", routingKey, evento);
        } catch (Exception ex) {
            log.warn("No se pudo publicar evento a RabbitMQ ({}), continuando: {}", routingKey, ex.getMessage());
        }
    }
}
