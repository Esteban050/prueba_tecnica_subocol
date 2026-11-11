package com.subocol.taller.agenda.dto;

import com.subocol.taller.agenda.domain.Cita;
import com.subocol.taller.agenda.domain.EstadoCita;
import com.subocol.taller.agenda.domain.TipoCita;
import java.time.LocalDateTime;

public class CitaResponse {

    private Long id;
    private Long ordenId;
    private Long operarioId;
    private String operarioNombre;
    private Long franjaCupoId;
    private String franjaHorario;
    private TipoCita tipo;
    private EstadoCita estado;
    private String motivoCancelacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCancelacion;

    public CitaResponse() {
    }

    public static CitaResponse from(Cita cita, String operarioNombre, String franjaHorario) {
        CitaResponse r = new CitaResponse();
        r.setId(cita.getId());
        r.setOrdenId(cita.getOrdenId());
        r.setOperarioId(cita.getOperarioId());
        r.setOperarioNombre(operarioNombre);
        r.setFranjaCupoId(cita.getFranjaCupoId());
        r.setFranjaHorario(franjaHorario);
        r.setTipo(cita.getTipo());
        r.setEstado(cita.getEstado());
        r.setMotivoCancelacion(cita.getMotivoCancelacion());
        r.setFechaCreacion(cita.getFechaCreacion());
        r.setFechaCancelacion(cita.getFechaCancelacion());
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

    public Long getFranjaCupoId() {
        return franjaCupoId;
    }

    public void setFranjaCupoId(Long franjaCupoId) {
        this.franjaCupoId = franjaCupoId;
    }

    public String getFranjaHorario() {
        return franjaHorario;
    }

    public void setFranjaHorario(String franjaHorario) {
        this.franjaHorario = franjaHorario;
    }

    public TipoCita getTipo() {
        return tipo;
    }

    public void setTipo(TipoCita tipo) {
        this.tipo = tipo;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }
}
