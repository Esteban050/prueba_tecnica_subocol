package com.subocol.taller.notificaciones;

import com.subocol.taller.notificaciones.domain.Notificacion;
import com.subocol.taller.notificaciones.event.CitaAgendadaEvent;
import com.subocol.taller.notificaciones.event.CitaCanceladaEvent;
import com.subocol.taller.notificaciones.repository.NotificacionRepository;
import com.subocol.taller.notificaciones.repository.ProcessedEventRepository;
import com.subocol.taller.notificaciones.service.NotificacionEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class NotificacionPolicyTest {

    @Autowired
    private NotificacionEventListener notificacionEventListener;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void setUp() {
        notificacionRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    @DisplayName("RF-22: Cita agendada genera aviso al cliente por WhatsApp")
    void testCitaAgendadaGeneraAvisoWhatsApp() {
        String eventId = UUID.randomUUID().toString();
        CitaAgendadaEvent event = new CitaAgendadaEvent(
                eventId,
                1L,
                10L,
                "3115551234",
                "Juan Pérez",
                "2026-11-12 09:00 - 11:00",
                "Carlos Operario",
                "ENTREGA",
                LocalDateTime.now()
        );

        notificacionEventListener.manejarCitaAgendada(event);

        List<Notificacion> notificaciones = notificacionRepository.findAll();
        assertEquals(1, notificaciones.size());
        Notificacion notif = notificaciones.get(0);
        assertTrue(notif.isEnviada(), "La notificación debe estar marcada como enviada");
        assertEquals("3115551234", notif.getDestinatarioTelefono());
        assertTrue(notif.getContenido().contains("AGENDADA"));
        assertTrue(notif.getContenido().contains("Carlos Operario"));
    }

    @Test
    @DisplayName("RF-24 Criterio de Aceptación: Cita cancelada por CIERRE_ORDEN NO envía aviso WhatsApp al cliente")
    void testCancelacionPorCierreOrdenNoEnviaAviso() {
        String eventId = UUID.randomUUID().toString();
        CitaCanceladaEvent event = new CitaCanceladaEvent(
                eventId,
                2L,
                20L,
                "3115551234",
                "2026-11-12 14:00 - 16:00",
                "CIERRE_ORDEN",
                LocalDateTime.now()
        );

        notificacionEventListener.manejarCitaCancelada(event);

        List<Notificacion> notificaciones = notificacionRepository.findAll();
        assertEquals(1, notificaciones.size());
        Notificacion notif = notificaciones.get(0);
        assertFalse(notif.isEnviada(), "Por política RF-24, NO se envía aviso al cliente al cerrar la orden");
        assertEquals("CIERRE_ORDEN", notif.getMotivo());
        assertNotNull(notif.getRazonOmision());
        assertTrue(notif.getRazonOmision().contains("CIERRE_ORDEN"));
    }

    @Test
    @DisplayName("RF-23 Criterio de Aceptación: Cita cancelada por motivo distinto a CIERRE_ORDEN SÍ envía aviso")
    void testCancelacionPorClienteSiEnviaAviso() {
        String eventId = UUID.randomUUID().toString();
        CitaCanceladaEvent event = new CitaCanceladaEvent(
                eventId,
                3L,
                30L,
                "3115551234",
                "2026-11-12 16:00 - 18:00",
                "CANCELADA_POR_CLIENTE",
                LocalDateTime.now()
        );

        notificacionEventListener.manejarCitaCancelada(event);

        List<Notificacion> notificaciones = notificacionRepository.findAll();
        assertEquals(1, notificaciones.size());
        Notificacion notif = notificaciones.get(0);
        assertTrue(notif.isEnviada(), "Al cancelarse por motivo del cliente, sí se envía WhatsApp");
        assertTrue(notif.getContenido().contains("CANCELADA"));
        assertTrue(notif.getContenido().contains("CANCELADA_POR_CLIENTE"));
    }

    @Test
    @DisplayName("RF-26 Criterio de Aceptación: Procesar el mismo evento dos veces no genera dos avisos")
    void testIdempotenciaNoDuplicaAvisos() {
        String eventId = "EVENT-NOTIFICATION-IDEMPOTENCY-001";
        CitaAgendadaEvent event = new CitaAgendadaEvent(
                eventId,
                4L,
                40L,
                "3009998877",
                "Ana Gómez",
                "2026-11-13 10:00 - 12:00",
                "Carlos Operario",
                "RECOGIDA",
                LocalDateTime.now()
        );

        // Primer procesamiento
        notificacionEventListener.manejarCitaAgendada(event);
        assertEquals(1, notificacionRepository.findAll().size());

        // Segundo procesamiento con el mismo eventId
        notificacionEventListener.manejarCitaAgendada(event);
        assertEquals(1, notificacionRepository.findAll().size(), "El total de avisos registrados debe mantenerse en 1");
    }
}
