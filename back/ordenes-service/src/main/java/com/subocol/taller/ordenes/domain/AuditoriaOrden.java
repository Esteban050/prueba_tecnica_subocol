package com.subocol.taller.ordenes.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_orden")
public class AuditoriaOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(name = "operario_id", nullable = false, length = 50)
    private String operarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", length = 30)
    private EstadoOrden estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 30)
    private EstadoOrden estadoNuevo;

    @Column(columnDefinition = "TEXT")
    private String razon;

    @Column(name = "momento", nullable = false, updatable = false)
    private LocalDateTime momento;

    public AuditoriaOrden() {
    }

    public AuditoriaOrden(Long ordenId, String operarioId, EstadoOrden estadoAnterior,
                          EstadoOrden estadoNuevo, String razon, LocalDateTime momento) {
        this.ordenId = ordenId;
        this.operarioId = operarioId != null ? operarioId : "SISTEMA";
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.razon = razon;
        this.momento = momento != null ? momento : LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.momento == null) {
            this.momento = LocalDateTime.now();
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

    public String getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(String operarioId) {
        this.operarioId = operarioId;
    }

    public EstadoOrden getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(EstadoOrden estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public EstadoOrden getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoOrden estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public void setMomento(LocalDateTime momento) {
        this.momento = momento;
    }
}
