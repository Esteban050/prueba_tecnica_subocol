package com.subocol.taller.agenda.controller;

import com.subocol.taller.agenda.dto.CrearFranjaRequest;
import com.subocol.taller.agenda.dto.FranjaResponse;
import com.subocol.taller.agenda.service.FranjaCupoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/franjas")
public class FranjaCupoController {

    private final FranjaCupoService franjaCupoService;

    public FranjaCupoController(FranjaCupoService franjaCupoService) {
        this.franjaCupoService = franjaCupoService;
    }

    @PostMapping
    public ResponseEntity<FranjaResponse> crearFranja(@Valid @RequestBody CrearFranjaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(franjaCupoService.crearFranja(request));
    }

    @GetMapping
    public ResponseEntity<List<FranjaResponse>> listarTodas() {
        return ResponseEntity.ok(franjaCupoService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<FranjaResponse>> listarDisponibles() {
        return ResponseEntity.ok(franjaCupoService.listarDisponibles());
    }

    @GetMapping("/operario/{operarioId}")
    public ResponseEntity<List<FranjaResponse>> listarPorOperario(@PathVariable Long operarioId) {
        return ResponseEntity.ok(franjaCupoService.listarPorOperario(operarioId));
    }
}
