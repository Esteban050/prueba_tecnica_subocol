package com.subocol.taller.agenda.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class OrdenCerradaEvent implements Serializable {
    private String eventId;
    private Long ordenId;
    private String operarioId;
    private LocalDateTime momento;
    private String razon;

    public OrdenCerradaEvent() {
    }

    public OrdenCerradaEvent(String eventId, Long ordenId, String operarioId, LocalDateTime momento, String razon) {
        this.eventId = eventId;
        this.ordenId = ordenId;
        this.operarioId = operarioId;
        this.momento = momento;
        this.razon = razon;
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

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }
}
