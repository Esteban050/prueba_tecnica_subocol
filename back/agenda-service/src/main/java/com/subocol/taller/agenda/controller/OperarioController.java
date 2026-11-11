package com.subocol.taller.agenda.controller;

import com.subocol.taller.agenda.domain.Operario;
import com.subocol.taller.agenda.service.OperarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/operarios")
public class OperarioController {

    private final OperarioService operarioService;

    public OperarioController(OperarioService operarioService) {
        this.operarioService = operarioService;
    }

    @PostMapping
    public ResponseEntity<Operario> registrar(@Valid @RequestBody Operario operario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operarioService.registrar(operario));
    }

    @GetMapping
    public ResponseEntity<List<Operario>> listarTodos() {
        return ResponseEntity.ok(operarioService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Operario>> listarActivos() {
        return ResponseEntity.ok(operarioService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(operarioService.buscarPorId(id));
    }
}
