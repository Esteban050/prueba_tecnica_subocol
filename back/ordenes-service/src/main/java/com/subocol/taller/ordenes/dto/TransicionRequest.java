package com.subocol.taller.ordenes.dto;

import com.subocol.taller.ordenes.domain.EstadoOrden;
import jakarta.validation.constraints.NotNull;

public class TransicionRequest {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoOrden estadoNuevo;

    private String razon;

    public TransicionRequest() {
    }

    public TransicionRequest(EstadoOrden estadoNuevo, String razon) {
        this.estadoNuevo = estadoNuevo;
        this.razon = razon;
    }

    public EstadoOrden getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(EstadoOrden estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }
}
