package com.subocol.taller.ordenes.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El cliente es obligatorio")
    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @NotNull(message = "El vehículo es obligatorio")
    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @NotBlank(message = "El trabajo a realizar es obligatorio")
    @Column(name = "trabajo_solicitado", nullable = false, columnDefinition = "TEXT")
    private String trabajoSolicitado;

    @Column(length = 100)
    private String aseguradora;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenOrden origen = OrigenOrden.OPERARIO;

    @Column(name = "id_externo", length = 100)
    private String idExterno;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado = EstadoOrden.RECIBIDA;

    @Column(name = "operario_id", length = 50)
    private String operarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    public Orden() {
    }

    public Orden(Long clienteId, Long vehiculoId, String trabajoSolicitado, String aseguradora,
                 OrigenOrden origen, String idExterno, String operarioId) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.trabajoSolicitado = trabajoSolicitado;
        this.aseguradora = aseguradora;
        this.origen = origen != null ? origen : OrigenOrden.OPERARIO;
        this.idExterno = idExterno;
        this.operarioId = operarioId;
        this.estado = EstadoOrden.RECIBIDA;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.fechaCreacion == null) {
            this.fechaCreacion = now;
        }
        this.fechaActualizacion = now;
        if (this.estado == null) {
            this.estado = EstadoOrden.RECIBIDA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
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
