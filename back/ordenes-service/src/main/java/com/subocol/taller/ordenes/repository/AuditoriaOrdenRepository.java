package com.subocol.taller.ordenes.repository;

import com.subocol.taller.ordenes.domain.AuditoriaOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaOrdenRepository extends JpaRepository<AuditoriaOrden, Long> {
    List<AuditoriaOrden> findByOrdenIdOrderByMomentoAsc(Long ordenId);
}
