package com.subocol.taller.agenda.controller;

import com.subocol.taller.agenda.dto.AgendarCitaRequest;
import com.subocol.taller.agenda.dto.CancelarCitaRequest;
import com.subocol.taller.agenda.dto.CitaResponse;
import com.subocol.taller.agenda.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> agendarCita(@Valid @RequestBody AgendarCitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.agendarCita(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CitaResponse> cancelarCita(
            @PathVariable Long id,
            @RequestParam(value = "motivo", required = false, defaultValue = "CANCELADA_POR_OPERARIO") String motivo) {
        return ResponseEntity.ok(citaService.cancelarCita(id, motivo));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelarCitaConBody(
            @PathVariable Long id,
            @Valid @RequestBody CancelarCitaRequest request) {
        return ResponseEntity.ok(citaService.cancelarCita(id, request.getMotivo()));
    }

    @GetMapping
    public ResponseEntity<List<CitaResponse>> listarCitas(
            @RequestParam(required = false) Long ordenId,
            @RequestParam(required = false) Long operarioId,
            @RequestParam(required = false) Long franjaId) {
        return ResponseEntity.ok(citaService.listarCitas(ordenId, operarioId, franjaId));
    }
}
