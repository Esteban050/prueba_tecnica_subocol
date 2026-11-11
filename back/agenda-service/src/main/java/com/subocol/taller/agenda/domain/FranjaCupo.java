package com.subocol.taller.agenda.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "franjas_cupo", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"operario_id", "inicio"})
})
public class FranjaCupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El operario es obligatorio")
    @Column(name = "operario_id", nullable = false)
    private Long operarioId;

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalDateTime inicio;

    @NotNull(message = "La fecha y hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fin;

    @NotNull(message = "El cupo máximo es obligatorio")
    @Column(name = "cupo_maximo", nullable = false)
    private Integer cupoMaximo = 2;

    @Column(nullable = false)
    private Integer ocupados = 0;

    public FranjaCupo() {
    }

    public FranjaCupo(Long operarioId, LocalDateTime inicio, LocalDateTime fin, Integer cupoMaximo) {
        this.operarioId = operarioId;
        this.inicio = inicio;
        this.fin = fin;
        this.cupoMaximo = cupoMaximo != null ? cupoMaximo : 2;
        this.ocupados = 0;
    }

    public boolean tieneCupoDisponible() {
        return this.ocupados < this.cupoMaximo;
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
}
