package com.subocol.taller.agenda.service;

import com.subocol.taller.agenda.domain.FranjaCupo;
import com.subocol.taller.agenda.domain.Operario;
import com.subocol.taller.agenda.dto.CrearFranjaRequest;
import com.subocol.taller.agenda.dto.FranjaResponse;
import com.subocol.taller.agenda.exception.RecursoNoEncontradoException;
import com.subocol.taller.agenda.repository.FranjaCupoRepository;
import com.subocol.taller.agenda.repository.OperarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FranjaCupoService {

    private final FranjaCupoRepository franjaCupoRepository;
    private final OperarioRepository operarioRepository;

    public FranjaCupoService(FranjaCupoRepository franjaCupoRepository, OperarioRepository operarioRepository) {
        this.franjaCupoRepository = franjaCupoRepository;
        this.operarioRepository = operarioRepository;
    }

    public FranjaResponse crearFranja(CrearFranjaRequest req) {
        Operario operario = operarioRepository.findById(req.getOperarioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Operario no encontrado con id: " + req.getOperarioId()));

        if (req.getFin().isBefore(req.getInicio())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la de inicio");
        }

        FranjaCupo franja = new FranjaCupo(
                operario.getId(),
                req.getInicio(),
                req.getFin(),
                req.getCupoMaximo() != null ? req.getCupoMaximo() : 2
        );

        FranjaCupo guardada = franjaCupoRepository.save(franja);
        return FranjaResponse.from(guardada, operario.getNombre());
    }

    @Transactional(readOnly = true)
    public List<FranjaResponse> listarTodas() {
        return franjaCupoRepository.findAll().stream().map(f -> {
            String opNombre = operarioRepository.findById(f.getOperarioId())
                    .map(Operario::getNombre)
                    .orElse("Operario #" + f.getOperarioId());
            return FranjaResponse.from(f, opNombre);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FranjaResponse> listarDisponibles() {
        return franjaCupoRepository.findFranjasDisponibles().stream().map(f -> {
            String opNombre = operarioRepository.findById(f.getOperarioId())
                    .map(Operario::getNombre)
                    .orElse("Operario #" + f.getOperarioId());
            return FranjaResponse.from(f, opNombre);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FranjaResponse> listarPorOperario(Long operarioId) {
        String opNombre = operarioRepository.findById(operarioId)
                .map(Operario::getNombre)
                .orElse("Operario #" + operarioId);

        return franjaCupoRepository.findByOperarioId(operarioId).stream()
                .map(f -> FranjaResponse.from(f, opNombre))
                .collect(Collectors.toList());
    }
}
