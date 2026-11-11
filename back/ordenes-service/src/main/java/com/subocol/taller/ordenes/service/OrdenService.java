package com.subocol.taller.ordenes.service;

import com.subocol.taller.ordenes.domain.*;
import com.subocol.taller.ordenes.dto.AuditoriaResponse;
import com.subocol.taller.ordenes.dto.CrearOrdenRequest;
import com.subocol.taller.ordenes.dto.OrdenResponse;
import com.subocol.taller.ordenes.dto.TransicionRequest;
import com.subocol.taller.ordenes.event.EstadoOrdenCambiadoEvent;
import com.subocol.taller.ordenes.event.OrdenCerradaEvent;
import com.subocol.taller.ordenes.exception.RecursoNoEncontradoException;
import com.subocol.taller.ordenes.exception.ReaperturaSinRazonException;
import com.subocol.taller.ordenes.exception.TransicionInvalidaException;
import com.subocol.taller.ordenes.repository.AuditoriaOrdenRepository;
import com.subocol.taller.ordenes.repository.ClienteRepository;
import com.subocol.taller.ordenes.repository.OrdenRepository;
import com.subocol.taller.ordenes.repository.VehiculoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository ordenRepository;
    private final ClienteRepository clienteRepository;
    private final VehiculoRepository vehiculoRepository;
    private final AuditoriaOrdenRepository auditoriaOrdenRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange:taller.exchange}")
    private String exchangeName;

    @Value("${app.rabbitmq.routing-key-estado:orden.estado.cambiado}")
    private String routingKeyEstado;

    @Value("${app.rabbitmq.routing-key-cerrada:orden.cerrada}")
    private String routingKeyCerrada;

    public OrdenService(OrdenRepository ordenRepository,
                        ClienteRepository clienteRepository,
                        VehiculoRepository vehiculoRepository,
                        AuditoriaOrdenRepository auditoriaOrdenRepository,
                        RabbitTemplate rabbitTemplate) {
        this.ordenRepository = ordenRepository;
        this.clienteRepository = clienteRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.auditoriaOrdenRepository = auditoriaOrdenRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public OrdenResponse crearOrden(CrearOrdenRequest req, String operarioId) {
        Cliente cliente = clienteRepository.findById(req.getClienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + req.getClienteId()));

        Vehiculo vehiculo = vehiculoRepository.findById(req.getVehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + req.getVehiculoId()));

        if (!vehiculo.getClienteId().equals(cliente.getId())) {
            throw new IllegalArgumentException("El vehículo con placa " + vehiculo.getPlaca() +
                    " no pertenece al cliente " + cliente.getNombre());
        }

        Orden orden = new Orden(
                cliente.getId(),
                vehiculo.getId(),
                req.getTrabajoSolicitado(),
                req.getAseguradora(),
                req.getOrigen(),
                req.getIdExterno(),
                operarioId
        );

        Orden guardada = ordenRepository.save(orden);

        // Registro de auditoría inicial
        AuditoriaOrden auditoria = new AuditoriaOrden(
                guardada.getId(),
                operarioId != null ? operarioId : "SISTEMA",
                null,
                guardada.getEstado(),
                "Creación inicial de la orden",
                LocalDateTime.now()
        );
        auditoriaOrdenRepository.save(auditoria);

        // Publicar evento de orden creada / estado inicial
        EstadoOrdenCambiadoEvent evento = new EstadoOrdenCambiadoEvent(
                UUID.randomUUID().toString(),
                guardada.getId(),
                null,
                guardada.getEstado(),
                "Orden creada",
                operarioId,
                LocalDateTime.now(),
                cliente.getTelefono(),
                cliente.getNombre()
        );
        publicarEvento(routingKeyEstado, evento);

        return OrdenResponse.from(guardada, cliente.getNombre(), cliente.getTelefono(),
                vehiculo.getPlaca(), vehiculo.getMarca() + " " + vehiculo.getModelo());
    }

    public OrdenResponse cambiarEstado(Long ordenId, TransicionRequest req, String operarioId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + ordenId));

        EstadoOrden estadoActual = orden.getEstado();
        EstadoOrden estadoDestino = req.getEstadoNuevo();

        if (estadoActual == estadoDestino) {
            throw new TransicionInvalidaException("La orden ya se encuentra en estado " + estadoActual);
        }

        // Validación de máquina de estados
        if (!estadoActual.esTransicionPermitida(estadoDestino)) {
            throw new TransicionInvalidaException(
                    String.format("Transición rechazada: No es permitido pasar de %s a %s", estadoActual, estadoDestino)
            );
        }

        // Validación de reapertura obligatoria con razón
        if (estadoActual == EstadoOrden.CERRADA && estadoDestino == EstadoOrden.EN_REPARACION) {
            if (req.getRazon() == null || req.getRazon().trim().isEmpty()) {
                throw new ReaperturaSinRazonException(
                        "Reapertura rechazada: Es obligatorio indicar una razón para reabrir una orden cerrada"
                );
            }
        }

        // Actualizar estado
        orden.setEstado(estadoDestino);
        orden.setOperarioId(operarioId);
        Orden actualizada = ordenRepository.save(orden);

        // Registrar auditoría en la misma transacción
        AuditoriaOrden auditoria = new AuditoriaOrden(
                actualizada.getId(),
                operarioId != null ? operarioId : "SISTEMA",
                estadoActual,
                estadoDestino,
                req.getRazon(),
                LocalDateTime.now()
        );
        auditoriaOrdenRepository.save(auditoria);

        Cliente cliente = clienteRepository.findById(actualizada.getClienteId()).orElse(null);
        Vehiculo vehiculo = vehiculoRepository.findById(actualizada.getVehiculoId()).orElse(null);

        String telefono = cliente != null ? cliente.getTelefono() : "";
        String nombre = cliente != null ? cliente.getNombre() : "";

        // Si la orden se cierra, publicar evento específico de cierre
        if (estadoDestino == EstadoOrden.CERRADA) {
            OrdenCerradaEvent cerradaEvent = new OrdenCerradaEvent(
                    UUID.randomUUID().toString(),
                    actualizada.getId(),
                    operarioId,
                    LocalDateTime.now(),
                    req.getRazon() != null ? req.getRazon() : "Cierre de orden"
            );
            publicarEvento(routingKeyCerrada, cerradaEvent);
        }

        // Siempre publicar cambio de estado
        EstadoOrdenCambiadoEvent estadoEvent = new EstadoOrdenCambiadoEvent(
                UUID.randomUUID().toString(),
                actualizada.getId(),
                estadoActual,
                estadoDestino,
                req.getRazon(),
                operarioId,
                LocalDateTime.now(),
                telefono,
                nombre
        );
        publicarEvento(routingKeyEstado, estadoEvent);

        return OrdenResponse.from(actualizada, nombre, telefono,
                vehiculo != null ? vehiculo.getPlaca() : "",
                vehiculo != null ? vehiculo.getMarca() + " " + vehiculo.getModelo() : "");
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorId(Long ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada con id: " + ordenId));

        Cliente cliente = clienteRepository.findById(orden.getClienteId()).orElse(null);
        Vehiculo vehiculo = vehiculoRepository.findById(orden.getVehiculoId()).orElse(null);

        return OrdenResponse.from(orden,
                cliente != null ? cliente.getNombre() : "",
                cliente != null ? cliente.getTelefono() : "",
                vehiculo != null ? vehiculo.getPlaca() : "",
                vehiculo != null ? vehiculo.getMarca() + " " + vehiculo.getModelo() : "");
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarTodas() {
        return ordenRepository.findAllByOrderByFechaCreacionDesc().stream().map(orden -> {
            Cliente c = clienteRepository.findById(orden.getClienteId()).orElse(null);
            Vehiculo v = vehiculoRepository.findById(orden.getVehiculoId()).orElse(null);
            return OrdenResponse.from(orden,
                    c != null ? c.getNombre() : "",
                    c != null ? c.getTelefono() : "",
                    v != null ? v.getPlaca() : "",
                    v != null ? v.getMarca() + " " + v.getModelo() : "");
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listarPorCliente(Long clienteId) {
        return ordenRepository.findByClienteId(clienteId).stream().map(orden -> {
            Cliente c = clienteRepository.findById(orden.getClienteId()).orElse(null);
            Vehiculo v = vehiculoRepository.findById(orden.getVehiculoId()).orElse(null);
            return OrdenResponse.from(orden,
                    c != null ? c.getNombre() : "",
                    c != null ? c.getTelefono() : "",
                    v != null ? v.getPlaca() : "",
                    v != null ? v.getMarca() + " " + v.getModelo() : "");
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> obtenerAuditoria(Long ordenId) {
        if (!ordenRepository.existsById(ordenId)) {
            throw new RecursoNoEncontradoException("Orden no encontrada con id: " + ordenId);
        }
        return auditoriaOrdenRepository.findByOrdenIdOrderByMomentoAsc(ordenId).stream()
                .map(AuditoriaResponse::from)
                .collect(Collectors.toList());
    }

    private void publicarEvento(String routingKey, Object evento) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, evento);
            log.info("Evento publicado con éxito [routingKey: {}]: {}", routingKey, evento);
        } catch (Exception ex) {
            log.warn("No se pudo publicar evento a RabbitMQ ({}), continuando transacción: {}",
                    routingKey, ex.getMessage());
        }
    }
}
