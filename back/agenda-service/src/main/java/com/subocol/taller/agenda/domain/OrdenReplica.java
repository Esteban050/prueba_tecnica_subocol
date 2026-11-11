package com.subocol.taller.agenda.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_replica")
public class OrdenReplica {

    @Id
    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(name = "cliente_telefono", length = 30)
    private String clienteTelefono;

    @Column(name = "cliente_nombre", length = 120)
    private String clienteNombre;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public OrdenReplica() {
    }

    public OrdenReplica(Long ordenId, String estado, String clienteTelefono, String clienteNombre) {
        this.ordenId = ordenId;
        this.estado = estado;
        this.clienteTelefono = clienteTelefono;
        this.clienteNombre = clienteNombre;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public boolean estaCerrada() {
        return "CERRADA".equalsIgnoreCase(this.estado);
    }

    public Long getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(Long ordenId) {
        this.ordenId = ordenId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
