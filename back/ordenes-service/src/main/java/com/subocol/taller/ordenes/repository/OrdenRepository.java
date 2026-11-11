package com.subocol.taller.ordenes.repository;

import com.subocol.taller.ordenes.domain.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
    List<Orden> findByClienteId(Long clienteId);
    List<Orden> findAllByOrderByFechaCreacionDesc();
}
