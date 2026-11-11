package com.subocol.taller.agenda.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CitaAgendadaEvent implements Serializable {
    private String eventId;
    private Long citaId;
    private Long ordenId;
    private String clienteTelefono;
    private String clienteNombre;
    private String franja;
    private String operarioNombre;
    private String tipo;
    private LocalDateTime momento;

    public CitaAgendadaEvent() {
    }

    public CitaAgendadaEvent(String eventId, Long citaId, Long ordenId, String clienteTelefono,
                             String clienteNombre, String franja, String operarioNombre,
                             String tipo, LocalDateTime momento) {
        this.eventId = eventId;
        this.citaId = citaId;
        this.ordenId = ordenId;
        this.clienteTelefono = clienteTelefono;
        this.clienteNombre = clienteNombre;
        this.franja = franja;
        this.operarioNombre = operarioNombre;
        this.tipo = tipo;
        this.momento = momento;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getCitaId() {
        return citaId;
    }

    public void setCitaId(Long citaId) {
        this.citaId = citaId;
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Long ordenId) {
        this.ordenId = ordenId;
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

    public String getFranja() {
        return franja;
    }

    public void setFranja(String franja) {
        this.franja = franja;
    }

    public String getOperarioNombre() {
        return operarioNombre;
    }

    public void setOperarioNombre(String operarioNombre) {
        this.operarioNombre = operarioNombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public void setMomento(LocalDateTime momento) {
        this.momento = momento;
    }
}
