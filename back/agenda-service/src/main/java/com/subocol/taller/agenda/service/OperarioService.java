package com.subocol.taller.agenda.service;

import com.subocol.taller.agenda.domain.Operario;
import com.subocol.taller.agenda.exception.RecursoNoEncontradoException;
import com.subocol.taller.agenda.repository.OperarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OperarioService {

    private final OperarioRepository operarioRepository;

    public OperarioService(OperarioRepository operarioRepository) {
        this.operarioRepository = operarioRepository;
    }

    public Operario registrar(Operario operario) {
        return operarioRepository.save(operario);
    }

    @Transactional(readOnly = true)
    public List<Operario> listarTodos() {
        return operarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Operario> listarActivos() {
        return operarioRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public Operario buscarPorId(Long id) {
        return operarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Operario no encontrado con id: " + id));
    }
}
