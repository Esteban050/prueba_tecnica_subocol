package com.subocol.taller.agenda.repository;

import com.subocol.taller.agenda.domain.Cita;
import com.subocol.taller.agenda.domain.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByOrdenId(Long ordenId);

    List<Cita> findByOrdenIdAndEstado(Long ordenId, EstadoCita estado);

    List<Cita> findByOperarioId(Long operarioId);

    List<Cita> findByFranjaCupoId(Long franjaCupoId);

    List<Cita> findAllByOrderByFechaCreacionDesc();
}
