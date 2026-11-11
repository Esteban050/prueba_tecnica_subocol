package com.subocol.taller.agenda.repository;

import com.subocol.taller.agenda.domain.Operario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperarioRepository extends JpaRepository<Operario, Long> {
    List<Operario> findByActivoTrue();
}
