package com.subocol.taller.notificaciones.controller;

import com.subocol.taller.notificaciones.domain.Notificacion;
import com.subocol.taller.notificaciones.repository.NotificacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;

    public NotificacionController(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listarTodas() {
        return ResponseEntity.ok(notificacionRepository.findAllByOrderByFechaRegistroDesc());
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<Notificacion>> listarPorOrden(@PathVariable Long ordenId) {
        return ResponseEntity.ok(notificacionRepository.findByOrdenIdOrderByFechaRegistroDesc(ordenId));
    }

    @GetMapping("/telefono/{telefono}")
    public ResponseEntity<List<Notificacion>> listarPorTelefono(@PathVariable String telefono) {
        return ResponseEntity.ok(notificacionRepository.findByDestinatarioTelefonoOrderByFechaRegistroDesc(telefono));
    }
}
