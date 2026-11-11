package com.subocol.taller.agenda;

import com.subocol.taller.agenda.domain.*;
import com.subocol.taller.agenda.dto.AgendarCitaRequest;
import com.subocol.taller.agenda.event.OrdenCerradaEvent;
import com.subocol.taller.agenda.repository.*;
import com.subocol.taller.agenda.service.CitaService;
import com.subocol.taller.agenda.service.OrdenEventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrdenCerradaEventListenerTest {

    @Autowired
    private OrdenEventListener ordenEventListener;

    @Autowired
    private CitaService citaService;

    @Autowired
    private FranjaCupoRepository franjaCupoRepository;

    @Autowired
    private OperarioRepository operarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private OrdenReplicaRepository ordenReplicaRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private Operario operario;

    @BeforeEach
    void setUp() {
        citaRepository.deleteAll();
        franjaCupoRepository.deleteAll();
        operarioRepository.deleteAll();
        ordenReplicaRepository.deleteAll();
        processedEventRepository.deleteAll();

        operario = operarioRepository.save(new Operario(null, "Pedro Supervisor", "Mecánica", true));
    }

    @Test
    @DisplayName("RF-18: Cierre de orden cancela citas pendientes con motivo CIERRE_ORDEN y libera cupos")
    void testCierreOrdenCancelaCitasYLiberanCupo() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(2);
        LocalDateTime fin = inicio.plusHours(2);
        FranjaCupo franja = franjaCupoRepository.save(new FranjaCupo(operario.getId(), inicio, fin, 5));

        long ordenId = 555L;

        // Agendar dos citas para la misma orden
        var c1 = citaService.agendarCita(new AgendarCitaRequest(ordenId, operario.getId(), franja.getId(), TipoCita.ENTREGA));
        var c2 = citaService.agendarCita(new AgendarCitaRequest(ordenId, operario.getId(), franja.getId(), TipoCita.RECOGIDA));

        FranjaCupo franjaOcupada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(2, franjaOcupada.getOcupados());

        // Simular llegada del evento OrdenCerrada
        String eventId = UUID.randomUUID().toString();
        OrdenCerradaEvent event = new OrdenCerradaEvent(eventId, ordenId, "OP-001", LocalDateTime.now(), "Cliente retiró vehículo");
        ordenEventListener.manejarOrdenCerrada(event);
        entityManager.clear();

        // Verificar que ambas citas ahora están en estado CANCELADA con motivo CIERRE_ORDEN
        List<Cita> citas = citaRepository.findByOrdenId(ordenId);
        assertEquals(2, citas.size());
        for (Cita c : citas) {
            assertEquals(EstadoCita.CANCELADA, c.getEstado());
            assertEquals("CIERRE_ORDEN", c.getMotivoCancelacion());
        }

        // Verificar que los cupos fueron liberados
        FranjaCupo franjaLiberada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(0, franjaLiberada.getOcupados(), "Los dos cupos deben ser liberados por el cierre de orden");
    }

    @Test
    @DisplayName("RF-26: Procesar dos veces el evento OrdenCerrada es idempotente y no duplica efectos")
    void testIdempotenciaOrdenCerrada() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(4);
        LocalDateTime fin = inicio.plusHours(2);
        FranjaCupo franja = franjaCupoRepository.save(new FranjaCupo(operario.getId(), inicio, fin, 5));

        long ordenId = 777L;
        citaService.agendarCita(new AgendarCitaRequest(ordenId, operario.getId(), franja.getId(), TipoCita.ENTREGA));

        String eventId = "EVENT-IDEMPOTENCY-123";
        OrdenCerradaEvent event = new OrdenCerradaEvent(eventId, ordenId, "OP-001", LocalDateTime.now(), "Cierre normal");

        // Primera ejecución
        ordenEventListener.manejarOrdenCerrada(event);
        FranjaCupo franjaPost1 = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(0, franjaPost1.getOcupados());

        // Segunda ejecución con el MISMO eventId
        ordenEventListener.manejarOrdenCerrada(event);
        FranjaCupo franjaPost2 = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(0, franjaPost2.getOcupados(), "No debe alterar negativamente los cupos");

        // Verificar registro de evento procesado
        assertTrue(processedEventRepository.existsById(eventId));
    }
}
