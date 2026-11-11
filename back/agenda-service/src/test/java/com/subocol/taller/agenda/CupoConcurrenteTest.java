package com.subocol.taller.agenda;

import com.subocol.taller.agenda.domain.FranjaCupo;
import com.subocol.taller.agenda.domain.Operario;
import com.subocol.taller.agenda.domain.TipoCita;
import com.subocol.taller.agenda.dto.AgendarCitaRequest;
import com.subocol.taller.agenda.exception.CupoAgotadoException;
import com.subocol.taller.agenda.repository.CitaRepository;
import com.subocol.taller.agenda.repository.FranjaCupoRepository;
import com.subocol.taller.agenda.repository.OperarioRepository;
import com.subocol.taller.agenda.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CupoConcurrenteTest {

    @Autowired
    private CitaService citaService;

    @Autowired
    private FranjaCupoRepository franjaCupoRepository;

    @Autowired
    private OperarioRepository operarioRepository;

    @Autowired
    private CitaRepository citaRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private Operario operarioGuardado;

    @BeforeEach
    void setUp() {
        citaRepository.deleteAll();
        franjaCupoRepository.deleteAll();
        operarioRepository.deleteAll();

        operarioGuardado = operarioRepository.save(new Operario(null, "Carlos Mecánico", "Mecánica General", true));
    }

    @Test
    @DisplayName("RF-15: Franja con cupo 2 rechaza tercer intento secuencial y mantiene ocupados en 2")
    void testRechazoCuandoCupoLlenoSecuencial() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fin = inicio.plusHours(2);
        FranjaCupo franja = franjaCupoRepository.save(new FranjaCupo(operarioGuardado.getId(), inicio, fin, 2));

        // Primer agendamiento
        citaService.agendarCita(new AgendarCitaRequest(100L, operarioGuardado.getId(), franja.getId(), TipoCita.ENTREGA));
        // Segundo agendamiento
        citaService.agendarCita(new AgendarCitaRequest(101L, operarioGuardado.getId(), franja.getId(), TipoCita.RECOGIDA));

        FranjaCupo consultada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(2, consultada.getOcupados());

        // Tercer agendamiento debe fallar por cupo agotado
        assertThrows(CupoAgotadoException.class, () ->
                citaService.agendarCita(new AgendarCitaRequest(102L, operarioGuardado.getId(), franja.getId(), TipoCita.ENTREGA))
        );

        consultada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(2, consultada.getOcupados(), "El cupo no debe superar el máximo permitido (2)");
    }

    @Test
    @DisplayName("RF-15 Criterio de Aceptación: Concurrencia real - 10 peticiones simultáneas para franja de cupo 2")
    void testConcurrenciaCupoAtomico() throws InterruptedException {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        LocalDateTime fin = inicio.plusHours(2);
        FranjaCupo franja = franjaCupoRepository.save(new FranjaCupo(operarioGuardado.getId(), inicio, fin, 2));

        int numeroHilos = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numeroHilos);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numeroHilos);

        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger rechazados = new AtomicInteger(0);

        for (int i = 0; i < numeroHilos; i++) {
            final long ordenId = 200L + i;
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Todos los hilos esperan el disparo simultáneo
                    citaService.agendarCita(new AgendarCitaRequest(ordenId, operarioGuardado.getId(), franja.getId(), TipoCita.ENTREGA));
                    exitos.incrementAndGet();
                } catch (CupoAgotadoException e) {
                    rechazados.incrementAndGet();
                } catch (Exception e) {
                    // Otros posibles errores
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Disparar todos los hilos al mismo instante
        startLatch.countDown();
        finishLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Exactamente 2 deben haber tenido éxito y 8 rechazados
        assertEquals(2, exitos.get(), "Exactamente 2 agendamientos deben ser exitosos");
        assertEquals(8, rechazados.get(), "Exactamente 8 agendamientos deben ser rechazados por cupo lleno");

        FranjaCupo franjaFinal = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(2, franjaFinal.getOcupados(), "El número de ocupados final debe ser exactamente 2");
    }

    @Test
    @DisplayName("RF-16: Al cancelar una cita, el cupo ocupado disminuye en 1 en la misma transacción")
    void testLiberacionDeCupoAlCancelar() {
        LocalDateTime inicio = LocalDateTime.now().plusHours(3);
        LocalDateTime fin = inicio.plusHours(4);
        FranjaCupo franja = franjaCupoRepository.save(new FranjaCupo(operarioGuardado.getId(), inicio, fin, 2));

        var cita = citaService.agendarCita(new AgendarCitaRequest(300L, operarioGuardado.getId(), franja.getId(), TipoCita.ENTREGA));
        FranjaCupo ocupada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(1, ocupada.getOcupados());

        // Cancelar cita
        citaService.cancelarCita(cita.getId(), "Cliente canceló por viaje");

        FranjaCupo liberada = franjaCupoRepository.findById(franja.getId()).orElseThrow();
        assertEquals(0, liberada.getOcupados(), "El cupo debe liberarse volviendo a 0");
    }
}
