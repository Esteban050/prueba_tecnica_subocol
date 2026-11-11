package com.subocol.taller.agenda.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelarCitaRequest {

    @NotBlank(message = "El motivo de cancelación es obligatorio")
    private String motivo;

    public CancelarCitaRequest() {
    }

    public CancelarCitaRequest(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
