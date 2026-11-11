package com.subocol.taller.agenda.dto;

import com.subocol.taller.agenda.domain.TipoCita;
import jakarta.validation.constraints.NotNull;

public class AgendarCitaRequest {

    @NotNull(message = "La ordenId es obligatoria")
    private Long ordenId;

    @NotNull(message = "El operarioId es obligatorio")
    private Long operarioId;

    @NotNull(message = "La franjaCupoId es obligatoria")
    private Long franjaCupoId;

    @NotNull(message = "El tipo de cita (ENTREGA / RECOGIDA) es obligatorio")
    private TipoCita tipo = TipoCita.ENTREGA;

    public AgendarCitaRequest() {
    }

    public AgendarCitaRequest(Long ordenId, Long operarioId, Long franjaCupoId, TipoCita tipo) {
        this.ordenId = ordenId;
        this.operarioId = operarioId;
        this.franjaCupoId = franjaCupoId;
        this.tipo = tipo != null ? tipo : TipoCita.ENTREGA;
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
}
