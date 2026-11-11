package com.subocol.taller.ordenes.dto;

import com.subocol.taller.ordenes.domain.EstadoOrden;
import com.subocol.taller.ordenes.domain.Orden;
import com.subocol.taller.ordenes.domain.OrigenOrden;
import java.time.LocalDateTime;

public class OrdenResponse {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private Long vehiculoId;
    private String vehiculoPlaca;
    private String vehiculoModelo;
    private String trabajoSolicitado;
    private String aseguradora;
    private OrigenOrden origen;
    private String idExterno;
    private EstadoOrden estado;
    private String operarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    public OrdenResponse() {
    }

    public static OrdenResponse from(Orden orden, String clienteNombre, String clienteTelefono,
                                     String vehiculoPlaca, String vehiculoModelo) {
        OrdenResponse r = new OrdenResponse();
        r.setId(orden.getId());
        r.setClienteId(orden.getClienteId());
        r.setClienteNombre(clienteNombre);
        r.setClienteTelefono(clienteTelefono);
        r.setVehiculoId(orden.getVehiculoId());
        r.setVehiculoPlaca(vehiculoPlaca);
        r.setVehiculoModelo(vehiculoModelo);
        r.setTrabajoSolicitado(orden.getTrabajoSolicitado());
        r.setAseguradora(orden.getAseguradora());
        r.setOrigen(orden.getOrigen());
        r.setIdExterno(orden.getIdExterno());
        r.setEstado(orden.getEstado());
        r.setOperarioId(orden.getOperarioId());
        r.setFechaCreacion(orden.getFechaCreacion());
        r.setFechaActualizacion(orden.getFechaActualizacion());
        return r;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getVehiculoPlaca() {
        return vehiculoPlaca;
    }

    public void setVehiculoPlaca(String vehiculoPlaca) {
        this.vehiculoPlaca = vehiculoPlaca;
    }

    public String getVehiculoModelo() {
        return vehiculoModelo;
    }

    public void setVehiculoModelo(String vehiculoModelo) {
        this.vehiculoModelo = vehiculoModelo;
    }

    public String getTrabajoSolicitado() {
        return trabajoSolicitado;
    }

    public void setTrabajoSolicitado(String trabajoSolicitado) {
        this.trabajoSolicitado = trabajoSolicitado;
    }

    public String getAseguradora() {
        return aseguradora;
    }

    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    public OrigenOrden getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenOrden origen) {
        this.origen = origen;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public String getOperarioId() {
        return operarioId;
    }

    public void setOperarioId(String operarioId) {
        this.operarioId = operarioId;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
