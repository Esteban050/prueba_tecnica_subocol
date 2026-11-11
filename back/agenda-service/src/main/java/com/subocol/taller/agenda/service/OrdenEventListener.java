package com.subocol.taller.agenda.service;

import com.subocol.taller.agenda.domain.*;
import com.subocol.taller.agenda.event.CitaCanceladaEvent;
import com.subocol.taller.agenda.event.EstadoOrdenCambiadoEvent;
import com.subocol.taller.agenda.event.OrdenCerradaEvent;
import com.subocol.taller.agenda.repository.CitaRepository;
import com.subocol.taller.agenda.repository.FranjaCupoRepository;
import com.subocol.taller.agenda.repository.OrdenReplicaRepository;
import com.subocol.taller.agenda.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class OrdenEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrdenEventListener.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CitaRepository citaRepository;
    private final FranjaCupoRepository franjaCupoRepository;
    private final OrdenReplicaRepository ordenReplicaRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:taller.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key-cita-cancelada:cita.cancelada}")
    private String routingKeyCitaCancelada;

    public OrdenEventListener(CitaRepository citaRepository,
                              FranjaCupoRepository franjaCupoRepository,
                              OrdenReplicaRepository ordenReplicaRepository,
                              ProcessedEventRepository processedEventRepository,
                              RabbitTemplate rabbitTemplate) {
        this.citaRepository = citaRepository;
        this.franjaCupoRepository = franjaCupoRepository;
        this.ordenReplicaRepository = ordenReplicaRepository;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * RF-18: Al recibir el evento de cierre de orden, cancela automáticamente sus citas pendientes
     * con motivo CIERRE_ORDEN y libera el cupo. Idempotente con eventId (RF-26).
     */
    @RabbitListener(queues = "${app.rabbitmq.queue-ordenes:agenda.ordenes.queue}")
    public void procesarEventoOrden(Object mensaje) {
        if (mensaje instanceof OrdenCerradaEvent cerradaEvent) {
            manejarOrdenCerrada(cerradaEvent);
        } else if (mensaje instanceof EstadoOrdenCambiadoEvent estadoEvent) {
            manejarCambioEstado(estadoEvent);
        } else {
            log.warn("Mensaje desconocido recibido en agenda.ordenes.queue: {}", mensaje);
        }
    }

    public void manejarOrdenCerrada(OrdenCerradaEvent event) {
        if (event == null || event.getEventId() == null) return;

        // Idempotencia: Verificar si el evento ya fue procesado
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Evento OrdenCerrada {} ya fue procesado previamente. Omitiendo duplicado.", event.getEventId());
            return;
        }

        log.info("Procesando cierre de orden #{} desde evento {}", event.getOrdenId(), event.getEventId());

        // Actualizar réplica local del estado
        OrdenReplica replica = ordenReplicaRepository.findById(event.getOrdenId())
                .orElse(new OrdenReplica(event.getOrdenId(), "CERRADA", null, null));
        replica.setEstado("CERRADA");
        replica.setFechaActualizacion(LocalDateTime.now());
        ordenReplicaRepository.save(replica);

        // RF-18: Cancelar citas pendientes de esta orden con motivo CIERRE_ORDEN
        List<Cita> citasPendientes = citaRepository.findByOrdenIdAndEstado(event.getOrdenId(), EstadoCita.AGENDADA);
        log.info("Se encontraron {} citas pendientes para cancelar por cierre de orden #{}",
                citasPendientes.size(), event.getOrdenId());

        for (Cita cita : citasPendientes) {
            cita.setEstado(EstadoCita.CANCELADA);
            cita.setMotivoCancelacion("CIERRE_ORDEN");
            cita.setFechaCancelacion(LocalDateTime.now());
            citaRepository.saveAndFlush(cita);

            // Liberar cupo de la franja de forma atómica
            franjaCupoRepository.decrementarCupoAtomico(cita.getFranjaCupoId());

            // Publicar CitaCancelada con motivo CIERRE_ORDEN
            FranjaCupo franja = franjaCupoRepository.findById(cita.getFranjaCupoId()).orElse(null);
            String franjaTexto = franja != null ? franja.getInicio().format(FORMATTER) + " - " + franja.getFin().format(FORMATTER) : "";

            CitaCanceladaEvent canceladaEvent = new CitaCanceladaEvent(
                    UUID.randomUUID().toString(),
                    cita.getId(),
                    cita.getOrdenId(),
                    replica.getClienteTelefono(),
                    franjaTexto,
                    "CIERRE_ORDEN",
                    LocalDateTime.now()
            );
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKeyCitaCancelada, canceladaEvent);
                log.info("Emitido evento CitaCancelada por CIERRE_ORDEN para cita #{}", cita.getId());
            } catch (Exception ex) {
                log.warn("Error enviando CitaCancelada a RabbitMQ: {}", ex.getMessage());
            }
        }

        // Registrar evento procesado para garantizar idempotencia
        processedEventRepository.save(new ProcessedEvent(event.getEventId(), "OrdenCerradaEvent"));
    }

    public void manejarCambioEstado(EstadoOrdenCambiadoEvent event) {
        if (event == null || event.getEventId() == null) return;

        log.info("Actualizando réplica local de orden #{}: estado = {}",
                event.getOrdenId(), event.getEstadoNuevo());

        OrdenReplica replica = ordenReplicaRepository.findById(event.getOrdenId())
                .orElse(new OrdenReplica(event.getOrdenId(), event.getEstadoNuevo().toString(),
                        event.getClienteTelefono(), event.getClienteNombre()));

        replica.setEstado(event.getEstadoNuevo().toString());
        if (event.getClienteTelefono() != null && !event.getClienteTelefono().isBlank()) {
            replica.setClienteTelefono(event.getClienteTelefono());
        }
        if (event.getClienteNombre() != null && !event.getClienteNombre().isBlank()) {
            replica.setClienteNombre(event.getClienteNombre());
        }
        replica.setFechaActualizacion(LocalDateTime.now());
        ordenReplicaRepository.save(replica);

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), "EstadoOrdenCambiadoEvent"));
    }
}
