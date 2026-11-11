package com.subocol.taller.agenda.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CrearFranjaRequest {

    @NotNull(message = "El operarioId es obligatorio")
    private Long operarioId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    private LocalDateTime fin;

    private Integer cupoMaximo = 2;

    public CrearFranjaRequest() {
    }

    public CrearFranjaRequest(Long operarioId, LocalDateTime inicio, LocalDateTime fin, Integer cupoMaximo) {
        this.operarioId = operarioId;
        this.inicio = inicio;
        this.fin = fin;
        this.cupoMaximo = cupoMaximo != null ? cupoMaximo : 2;
    }

    public Long getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(Long operarioId) {
        this.operarioId = operarioId;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public Integer getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(Integer cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }
}
