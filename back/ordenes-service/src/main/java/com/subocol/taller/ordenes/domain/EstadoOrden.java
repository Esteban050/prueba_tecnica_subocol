package com.subocol.taller.ordenes.domain;

public enum EstadoOrden {
    RECIBIDA,
    EN_DIAGNOSTICO,
    EN_REPARACION,
    REPARADA,
    CERRADA;

    public boolean esTransicionPermitida(EstadoOrden destino) {
        if (destino == null) return false;
        return switch (this) {
            case RECIBIDA -> destino == EN_DIAGNOSTICO;
            case EN_DIAGNOSTICO -> destino == EN_REPARACION;
            case EN_REPARACION -> destino == REPARADA;
            case REPARADA -> destino == CERRADA;
            case CERRADA -> destino == EN_REPARACION; // Reabrir orden
        };
    }
}
