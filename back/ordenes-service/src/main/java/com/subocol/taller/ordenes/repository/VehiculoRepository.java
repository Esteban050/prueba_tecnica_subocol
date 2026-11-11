package com.subocol.taller.ordenes.repository;

import com.subocol.taller.ordenes.domain.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    List<Vehiculo> findByClienteId(Long clienteId);
    Optional<Vehiculo> findByPlaca(String placa);
    boolean existsByPlaca(String placa);
}
