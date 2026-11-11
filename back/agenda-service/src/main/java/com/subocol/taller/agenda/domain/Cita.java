package com.subocol.taller.agenda.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La orden es obligatoria")
    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @NotNull(message = "El operario es obligatorio")
    @Column(name = "operario_id", nullable = false)
    private Long operarioId;

    @NotNull(message = "La franja es obligatoria")
    @Column(name = "franja_cupo_id", nullable = false)
    private Long franjaCupoId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoCita tipo = TipoCita.ENTREGA;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.AGENDADA;

    @Column(name = "motivo_cancelacion", length = 150)
    private String motivoCancelacion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    public Cita() {
    }

    public Cita(Long ordenId, Long operarioId, Long franjaCupoId, TipoCita tipo) {
        this.ordenId = ordenId;
        this.operarioId = operarioId;
        this.franjaCupoId = franjaCupoId;
        this.tipo = tipo != null ? tipo : TipoCita.ENTREGA;
        this.estado = EstadoCita.AGENDADA;
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoCita.AGENDADA;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Long ordenId) {
        this.ordenId = ordenId;
    }

    public Long getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(Long operarioId) {
        this.operarioId = operarioId;
    }

    public Long getFranjaCupoId() {
        return franjaCupoId;
    }

    public void setFranjaCupoId(Long franjaCupoId) {
        this.franjaCupoId = franjaCupoId;
    }

    public TipoCita getTipo() {
        return tipo;
    }

    public void setTipo(TipoCita tipo) {
        this.tipo = tipo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }
}
