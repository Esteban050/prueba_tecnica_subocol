package com.subocol.taller.agenda.dto;

import com.subocol.taller.agenda.domain.FranjaCupo;
import java.time.LocalDateTime;

public class FranjaResponse {

    private Long id;
    private Long operarioId;
    private String operarioNombre;
    private LocalDateTime inicio;
    private LocalDateTime fin;
    private Integer cupoMaximo;
    private Integer ocupados;
    private Integer disponibles;
    private boolean llena;

    public FranjaResponse() {
    }

    public static FranjaResponse from(FranjaCupo f, String operarioNombre) {
        FranjaResponse r = new FranjaResponse();
        r.setId(f.getId());
        r.setOperarioId(f.getOperarioId());
        r.setOperarioNombre(operarioNombre);
        r.setInicio(f.getInicio());
        r.setFin(f.getFin());
        r.setCupoMaximo(f.getCupoMaximo());
        r.setOcupados(f.getOcupados());
        r.setDisponibles(Math.max(0, f.getCupoMaximo() - f.getOcupados()));
        r.setLlena(f.getOcupados() >= f.getCupoMaximo());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(Long operarioId) {
        this.operarioId = operarioId;
    }

    public String getOperarioNombre() {
        return operarioNombre;
    }

    public void setOperarioNombre(String operarioNombre) {
        this.operarioNombre = operarioNombre;
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

    public Integer getOcupados() {
        return ocupados;
    }

    public void setOcupados(Integer ocupados) {
        this.ocupados = ocupados;
    }

    public Integer getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(Integer disponibles) {
        this.disponibles = disponibles;
    }

    public boolean isLlena() {
        return llena;
    }

    public void setLlena(boolean llena) {
        this.llena = llena;
    }
}
