package com.subocol.taller.ordenes.service;

import com.subocol.taller.ordenes.domain.Vehiculo;
import com.subocol.taller.ordenes.exception.RecursoNoEncontradoException;
import com.subocol.taller.ordenes.repository.ClienteRepository;
import com.subocol.taller.ordenes.repository.VehiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ClienteRepository clienteRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, ClienteRepository clienteRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.clienteRepository = clienteRepository;
    }

    public Vehiculo registrar(Vehiculo vehiculo) {
        if (!clienteRepository.existsById(vehiculo.getClienteId())) {
            throw new RecursoNoEncontradoException("No existe el cliente con id: " + vehiculo.getClienteId());
        }
        if (vehiculoRepository.existsByPlaca(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa " + vehiculo.getPlaca());
        }
        return vehiculoRepository.save(vehiculo);
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarPorCliente(Long clienteId) {
        return vehiculoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public List<Vehiculo> listarTodos() {
        return vehiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Vehiculo buscarPorId(Long id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado con id: " + id));
    }
}
