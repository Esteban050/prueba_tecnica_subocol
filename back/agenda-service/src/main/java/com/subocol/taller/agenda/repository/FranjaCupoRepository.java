package com.subocol.taller.agenda.repository;

import com.subocol.taller.agenda.domain.FranjaCupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FranjaCupoRepository extends JpaRepository<FranjaCupo, Long> {

    List<FranjaCupo> findByOperarioId(Long operarioId);

    List<FranjaCupo> findByInicioAfterOrderByInicioAsc(LocalDateTime fecha);

    @Query("SELECT f FROM FranjaCupo f WHERE f.ocupados < f.cupoMaximo ORDER BY f.inicio ASC")
    List<FranjaCupo> findFranjasDisponibles();

    Optional<FranjaCupo> findByOperarioIdAndInicio(Long operarioId, LocalDateTime inicio);

    /**
     * Operación atómica de incremento de cupo (RF-15):
     * Retorna 1 si reservó exitosamente, o 0 si la franja ya alcanzó su cupo máximo.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FranjaCupo f SET f.ocupados = f.ocupados + 1 WHERE f.id = :id AND f.ocupados < f.cupoMaximo")
    int incrementarCupoAtomico(@Param("id") Long id);

    /**
     * Operación atómica de decremento de cupo al cancelar:
     * Retorna 1 si liberó cupo, o 0 si ya estaba en 0.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FranjaCupo f SET f.ocupados = f.ocupados - 1 WHERE f.id = :id AND f.ocupados > 0")
    int decrementarCupoAtomico(@Param("id") Long id);
}
