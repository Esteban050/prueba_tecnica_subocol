package com.subocol.taller.ordenes.controller;

import com.subocol.taller.ordenes.dto.AuditoriaResponse;
import com.subocol.taller.ordenes.dto.CrearOrdenRequest;
import com.subocol.taller.ordenes.dto.OrdenResponse;
import com.subocol.taller.ordenes.dto.TransicionRequest;
import com.subocol.taller.ordenes.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @PostMapping("/ordenes")
    public ResponseEntity<OrdenResponse> crearOrden(
            @Valid @RequestBody CrearOrdenRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "OP-001") String operarioId) {
        OrdenResponse creada = ordenService.crearOrden(request, operarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @GetMapping("/ordenes")
    public ResponseEntity<List<OrdenResponse>> listarTodas() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @GetMapping("/ordenes/{id}")
    public ResponseEntity<OrdenResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.buscarPorId(id));
    }

    @GetMapping("/clientes/{clienteId}/ordenes")
    public ResponseEntity<List<OrdenResponse>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(ordenService.listarPorCliente(clienteId));
    }

    @PostMapping("/ordenes/{id}/transiciones")
    public ResponseEntity<OrdenResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody TransicionRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "OP-001") String operarioId) {
        OrdenResponse actualizada = ordenService.cambiarEstado(id, request, operarioId);
        return ResponseEntity.ok(actualizada);
    }

    @GetMapping("/ordenes/{id}/auditoria")
    public ResponseEntity<List<AuditoriaResponse>> obtenerAuditoria(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.obtenerAuditoria(id));
    }
}
