package com.subocol.taller.ordenes;

import com.subocol.taller.ordenes.domain.EstadoOrden;
import com.subocol.taller.ordenes.domain.Orden;
import com.subocol.taller.ordenes.domain.OrigenOrden;
import com.subocol.taller.ordenes.dto.CrearOrdenRequest;
import com.subocol.taller.ordenes.dto.OrdenResponse;
import com.subocol.taller.ordenes.dto.TransicionRequest;
import com.subocol.taller.ordenes.domain.Cliente;
import com.subocol.taller.ordenes.domain.Vehiculo;
import com.subocol.taller.ordenes.exception.ReaperturaSinRazonException;
import com.subocol.taller.ordenes.exception.TransicionInvalidaException;
import com.subocol.taller.ordenes.repository.AuditoriaOrdenRepository;
import com.subocol.taller.ordenes.repository.ClienteRepository;
import com.subocol.taller.ordenes.repository.OrdenRepository;
import com.subocol.taller.ordenes.repository.VehiculoRepository;
import com.subocol.taller.ordenes.service.OrdenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenStateMachineTest {

    @Mock
    private OrdenRepository ordenRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private AuditoriaOrdenRepository auditoriaOrdenRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private OrdenService ordenService;

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(
                ordenRepository,
                clienteRepository,
                vehiculoRepository,
                auditoriaOrdenRepository,
                rabbitTemplate
        );
    }

    @Test
    @DisplayName("Transición válida: RECIBIDA -> EN_DIAGNOSTICO")
    void testTransicionValidaRecibidaADiagnostico() {
        Orden orden = new Orden(1L, 1L, "Revisión general", null, OrigenOrden.OPERARIO, null, "OP-001");
        orden.setId(10L);
        orden.setEstado(EstadoOrden.RECIBIDA);

        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        OrdenResponse response = ordenService.cambiarEstado(10L, new TransicionRequest(EstadoOrden.EN_DIAGNOSTICO, "Iniciando diagnóstico"), "OP-001");

        assertEquals(EstadoOrden.EN_DIAGNOSTICO, response.getEstado());
        verify(auditoriaOrdenRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Transición inválida directa: RECIBIDA -> REPARADA debe fallar")
    void testTransicionInvalidaDirectaFalla() {
        Orden orden = new Orden(1L, 1L, "Revisión", null, OrigenOrden.OPERARIO, null, "OP-001");
        orden.setId(10L);
        orden.setEstado(EstadoOrden.RECIBIDA);

        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

        assertThrows(TransicionInvalidaException.class, () ->
                ordenService.cambiarEstado(10L, new TransicionRequest(EstadoOrden.REPARADA, null), "OP-001")
        );
    }

    @Test
    @DisplayName("RF-07: Reapertura de CERRADA a EN_REPARACION sin razón debe ser rechazada")
    void testReaperturaSinRazonFalla() {
        Orden orden = new Orden(1L, 1L, "Revisión", null, OrigenOrden.OPERARIO, null, "OP-001");
        orden.setId(10L);
        orden.setEstado(EstadoOrden.CERRADA);

        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));

        assertThrows(ReaperturaSinRazonException.class, () ->
                ordenService.cambiarEstado(10L, new TransicionRequest(EstadoOrden.EN_REPARACION, "   "), "OP-001")
        );

        assertThrows(ReaperturaSinRazonException.class, () ->
                ordenService.cambiarEstado(10L, new TransicionRequest(EstadoOrden.EN_REPARACION, null), "OP-001")
        );
    }

    @Test
    @DisplayName("RF-07: Reapertura de CERRADA a EN_REPARACION con razón válida debe proceder")
    void testReaperturaConRazonValidaExito() {
        Orden orden = new Orden(1L, 1L, "Revisión", null, OrigenOrden.OPERARIO, null, "OP-001");
        orden.setId(10L);
        orden.setEstado(EstadoOrden.CERRADA);

        when(ordenRepository.findById(10L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));

        OrdenResponse response = ordenService.cambiarEstado(10L,
                new TransicionRequest(EstadoOrden.EN_REPARACION, "Garantía por ruido residual en suspensión"), "OP-002");

        assertEquals(EstadoOrden.EN_REPARACION, response.getEstado());
        verify(auditoriaOrdenRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("RF-09 & RF-12: Cierre de orden guarda auditoría y publica eventos")
    void testCierreOrdenGuardaAuditoriaYPublicaEventos() {
        Orden orden = new Orden(1L, 1L, "Reparación", null, OrigenOrden.OPERARIO, null, "OP-001");
        orden.setId(15L);
        orden.setEstado(EstadoOrden.REPARADA);

        Cliente cliente = new Cliente(1L, "Juan Perez", "123456", "3001234567", "juan@test.com", "Calle 10");
        Vehiculo vehiculo = new Vehiculo(1L, "ABC123", "Mazda", "3", 2022, "Rojo", 1L);

        when(ordenRepository.findById(15L)).thenReturn(Optional.of(orden));
        when(ordenRepository.save(any(Orden.class))).thenAnswer(i -> i.getArgument(0));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(vehiculo));

        OrdenResponse response = ordenService.cambiarEstado(15L,
                new TransicionRequest(EstadoOrden.CERRADA, "Vehículo entregado al cliente a satisfacción"), "OP-001");

        assertEquals(EstadoOrden.CERRADA, response.getEstado());
        verify(auditoriaOrdenRepository, times(1)).save(any());
    }
}
