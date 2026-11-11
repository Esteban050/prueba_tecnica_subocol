package com.subocol.taller.ordenes.dto;

import com.subocol.taller.ordenes.domain.OrigenOrden;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CrearOrdenRequest {

    @NotNull(message = "El clienteId es obligatorio")
    private Long clienteId;

    @NotNull(message = "El vehiculoId es obligatorio")
    private Long vehiculoId;

    @NotBlank(message = "El trabajo solicitado es obligatorio")
    private String trabajoSolicitado;

    private String aseguradora;

    private OrigenOrden origen = OrigenOrden.OPERARIO;

    private String idExterno;

    public CrearOrdenRequest() {
    }

    public CrearOrdenRequest(Long clienteId, Long vehiculoId, String trabajoSolicitado,
                             String aseguradora, OrigenOrden origen, String idExterno) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.trabajoSolicitado = trabajoSolicitado;
        this.aseguradora = aseguradora;
        this.origen = origen != null ? origen : OrigenOrden.OPERARIO;
        this.idExterno = idExterno;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
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
}
