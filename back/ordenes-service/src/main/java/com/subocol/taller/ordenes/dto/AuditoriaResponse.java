package com.subocol.taller.ordenes.dto;

import com.subocol.taller.ordenes.domain.AuditoriaOrden;
import com.subocol.taller.ordenes.domain.EstadoOrden;
import java.time.LocalDateTime;

public class AuditoriaResponse {

    private Long id;
    private Long ordenId;
    private String operarioId;
    private EstadoOrden estadoAnterior;
    private EstadoOrden estadoNuevo;
    private String razon;
    private LocalDateTime momento;

    public AuditoriaResponse() {
    }

    public static AuditoriaResponse from(AuditoriaOrden a) {
        AuditoriaResponse r = new AuditoriaResponse();
        r.setId(a.getId());
        r.setOrdenId(a.getOrdenId());
        r.setOperarioId(a.getOperarioId());
        r.setEstadoAnterior(a.getEstadoAnterior());
        r.setEstadoNuevo(a.getEstadoNuevo());
        r.setRazon(a.getRazon());
        r.setMomento(a.getMomento());
        return r;
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
