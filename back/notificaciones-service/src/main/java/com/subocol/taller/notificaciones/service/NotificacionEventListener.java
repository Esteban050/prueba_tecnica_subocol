package com.subocol.taller.notificaciones.service;

import com.subocol.taller.notificaciones.domain.Notificacion;
import com.subocol.taller.notificaciones.domain.ProcessedEvent;
import com.subocol.taller.notificaciones.event.CitaAgendadaEvent;
import com.subocol.taller.notificaciones.event.CitaCanceladaEvent;
import com.subocol.taller.notificaciones.repository.NotificacionRepository;
import com.subocol.taller.notificaciones.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificacionEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificacionEventListener.class);

    private final NotificacionRepository notificacionRepository;
    private final ProcessedEventRepository processedEventRepository;

    public NotificacionEventListener(NotificacionRepository notificacionRepository,
                                     ProcessedEventRepository processedEventRepository) {
        this.notificacionRepository = notificacionRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue-citas:notificaciones.citas.queue}")
    public void procesarEventoCita(Object mensaje) {
        if (mensaje instanceof CitaAgendadaEvent agendadaEvent) {
            manejarCitaAgendada(agendadaEvent);
        } else if (mensaje instanceof CitaCanceladaEvent canceladaEvent) {
            manejarCitaCancelada(canceladaEvent);
        } else {
            log.warn("Mensaje desconocido recibido en notificaciones.citas.queue: {}", mensaje);
        }
    }

    /**
     * RF-22: Al agendarse una cita, enviar aviso por WhatsApp (simulado con log).
     * RF-26: Idempotente por eventId.
     */
    public void manejarCitaAgendada(CitaAgendadaEvent event) {
        if (event == null || event.getEventId() == null) return;

        // Idempotencia: Verificar si ya se procesó este evento
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Evento CitaAgendada {} ya procesado previamente. Omitiendo duplicado.", event.getEventId());
            return;
        }

        String cliente = (event.getClienteNombre() != null && !event.getClienteNombre().isBlank())
                ? event.getClienteNombre() : "Estimado cliente";
        String telefono = event.getClienteTelefono() != null ? event.getClienteTelefono() : "No especificado";

        String contenido = String.format(
                "¡Hola %s! Su cita para la orden #%d (%s) ha sido AGENDADA con éxito para la franja %s con el operario %s. Taller Automotriz Subocol.",
                cliente,
                event.getOrdenId(),
                event.getTipo() != null ? event.getTipo() : "ENTREGA",
                event.getFranja(),
                event.getOperarioNombre() != null ? event.getOperarioNombre() : "Asignado"
        );

        // Simular WhatsApp con Log estructurado (RF-22)
        log.info("==========================================================================");
        log.info("[WHATSAPP-SIMULATOR] -> [ENVIADO CON ÉXITO]");
        log.info("Para: +57 {}", telefono);
        log.info("Mensaje: \"{}\"", contenido);
        log.info("==========================================================================");

        // Guardar registro en base de datos (RF-25)
        Notificacion notificacion = new Notificacion(
                event.getEventId(),
                event.getOrdenId(),
                event.getCitaId(),
                "WHATSAPP",
                telefono,
                cliente,
                contenido,
                "CitaAgendada",
                null,
                true,
                null
        );
        notificacionRepository.save(notificacion);

        // Marcar evento como procesado
        processedEventRepository.save(new ProcessedEvent(event.getEventId(), "CitaAgendadaEvent"));
    }

    /**
     * RF-23: Al cancelarse por motivo distinto a CIERRE_ORDEN, avisar al cliente por WhatsApp.
     * RF-24: Si es CIERRE_ORDEN, NO enviar aviso (cliente ya recogió su vehículo).
     * RF-26: Idempotente por eventId.
     */
    public void manejarCitaCancelada(CitaCanceladaEvent event) {
        if (event == null || event.getEventId() == null) return;

        // Idempotencia
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Evento CitaCancelada {} ya procesado previamente. Omitiendo duplicado.", event.getEventId());
            return;
        }

        String motivo = event.getMotivo() != null ? event.getMotivo() : "CANCELADA";
        String telefono = event.getClienteTelefono() != null ? event.getClienteTelefono() : "No especificado";

        // RF-24: Regla de política - Si el motivo es CIERRE_ORDEN, NO enviar WhatsApp
        if ("CIERRE_ORDEN".equalsIgnoreCase(motivo)) {
            log.info("--------------------------------------------------------------------------");
            log.info("[WHATSAPP-POLICY] -> [OMITIDO POR POLÍTICA RF-24]");
            log.info("Cita #{} para orden #{}: Motivo CIERRE_ORDEN.", event.getCitaId(), event.getOrdenId());
            log.info("No se envía WhatsApp al cliente porque ya retiró su vehículo a satisfacción.");
            log.info("--------------------------------------------------------------------------");

            Notificacion omitida = new Notificacion(
                    event.getEventId(),
                    event.getOrdenId(),
                    event.getCitaId(),
                    "WHATSAPP",
                    telefono,
                    null,
                    "Mensaje omitido por política de negocio",
                    "CitaCancelada",
                    motivo,
                    false,
                    "CIERRE_ORDEN: El cliente ya retiró su vehículo (RF-24)"
            );
            notificacionRepository.save(omitida);

            processedEventRepository.save(new ProcessedEvent(event.getEventId(), "CitaCanceladaEvent"));
            return;
        }

        // RF-23: Cancelación por otro motivo (cliente, taller, reprogramación) -> Sí se envía WhatsApp
        String contenido = String.format(
                "¡Hola! Le informamos que su cita para la orden #%d (%s) ha sido CANCELADA. Motivo: %s. Taller Automotriz Subocol.",
                event.getOrdenId(),
                event.getFranja() != null ? event.getFranja() : "",
                motivo
        );

        log.info("==========================================================================");
        log.info("[WHATSAPP-SIMULATOR] -> [ENVIADO CON ÉXITO]");
        log.info("Para: +57 {}", telefono);
        log.info("Mensaje: \"{}\"", contenido);
        log.info("==========================================================================");

        Notificacion enviada = new Notificacion(
                event.getEventId(),
                event.getOrdenId(),
                event.getCitaId(),
                "WHATSAPP",
                telefono,
                null,
                contenido,
                "CitaCancelada",
                motivo,
                true,
                null
        );
        notificacionRepository.save(enviada);

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), "CitaCanceladaEvent"));
    }
}
