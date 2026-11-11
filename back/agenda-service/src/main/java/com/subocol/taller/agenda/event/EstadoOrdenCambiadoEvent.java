package com.subocol.taller.agenda.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class EstadoOrdenCambiadoEvent implements Serializable {
    private String eventId;
    private Long ordenId;
    private String estadoAnterior;
    private String estadoNuevo;
    private String razon;
    private String operarioId;
    private LocalDateTime momento;
    private String clienteTelefono;
    private String clienteNombre;

    public EstadoOrdenCambiadoEvent() {
    }

    public EstadoOrdenCambiadoEvent(String eventId, Long ordenId, String estadoAnterior,
                                    String estadoNuevo, String razon, String operarioId,
                                    LocalDateTime momento, String clienteTelefono, String clienteNombre) {
        this.eventId = eventId;
        this.ordenId = ordenId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.razon = razon;
        this.operarioId = operarioId;
        this.momento = momento;
        this.clienteTelefono = clienteTelefono;
        this.clienteNombre = clienteNombre;
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

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public String getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(String operarioId) {
        this.operarioId = operarioId;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public void setMomento(LocalDateTime momento) {
        this.momento = momento;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
}
