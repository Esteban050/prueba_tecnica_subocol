package com.subocol.taller.notificaciones.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CitaCanceladaEvent implements Serializable {
    private String eventId;
    private Long citaId;
    private Long ordenId;
    private String clienteTelefono;
    private String franja;
    private String motivo;
    private LocalDateTime momento;

    public CitaCanceladaEvent() {
    }

    public CitaCanceladaEvent(String eventId, Long citaId, Long ordenId, String clienteTelefono,
                              String franja, String motivo, LocalDateTime momento) {
        this.eventId = eventId;
        this.citaId = citaId;
        this.ordenId = ordenId;
        this.clienteTelefono = clienteTelefono;
        this.franja = franja;
        this.motivo = motivo;
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

    public String getFranja() {
        return franja;
    }

    public void setFranja(String franja) {
        this.franja = franja;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getMomento() {
        return momento;
    }

    public void setMomento(LocalDateTime momento) {
        this.momento = momento;
    }
}
