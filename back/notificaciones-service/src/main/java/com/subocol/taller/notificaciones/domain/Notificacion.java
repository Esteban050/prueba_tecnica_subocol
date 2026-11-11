package com.subocol.taller.notificaciones.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", length = 100)
    private String eventId;

    @Column(name = "orden_id")
    private Long ordenId;

    @Column(name = "cita_id")
    private Long citaId;

    @Column(nullable = false, length = 30)
    private String canal = "WHATSAPP";

    @Column(name = "destinatario_telefono", length = 30)
    private String destinatarioTelefono;

    @Column(name = "destinatario_nombre", length = 120)
    private String destinatarioNombre;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "tipo_evento", length = 50)
    private String tipoEvento;

    @Column(length = 100)
    private String motivo;

    @Column(nullable = false)
    private boolean enviada = false;

    @Column(name = "razon_omision", length = 200)
    private String razonOmision;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public Notificacion() {
    }

    public Notificacion(String eventId, Long ordenId, Long citaId, String canal,
                        String destinatarioTelefono, String destinatarioNombre,
                        String contenido, String tipoEvento, String motivo,
                        boolean enviada, String razonOmision) {
        this.eventId = eventId;
        this.ordenId = ordenId;
        this.citaId = citaId;
        this.canal = canal != null ? canal : "WHATSAPP";
        this.destinatarioTelefono = destinatarioTelefono;
        this.destinatarioNombre = destinatarioNombre;
        this.contenido = contenido;
        this.tipoEvento = tipoEvento;
        this.motivo = motivo;
        this.enviada = enviada;
        this.razonOmision = razonOmision;
        this.fechaRegistro = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Long ordenId) {
        this.ordenId = ordenId;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getDestinatarioTelefono() {
        return destinatarioTelefono;
    }

    public void setDestinatarioTelefono(String destinatarioTelefono) {
        this.destinatarioTelefono = destinatarioTelefono;
    }

    public String getDestinatarioNombre() {
        return destinatarioNombre;
    }

    public void setDestinatarioNombre(String destinatarioNombre) {
        this.destinatarioNombre = destinatarioNombre;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isEnviada() {
        return enviada;
    }

    public void setEnviada(boolean enviada) {
        this.enviada = enviada;
    }

    public String getRazonOmision() {
        return razonOmision;
    }

    public void setRazonOmision(String razonOmision) {
        this.razonOmision = razonOmision;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
