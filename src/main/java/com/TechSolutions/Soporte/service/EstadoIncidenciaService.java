package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoIncidenciaService {

    @Autowired
    private EstadoIncidenciaRepository estadoIncidenciaRepository;

    public List<EstadoIncidencia> findAllEstados() {
        return estadoIncidenciaRepository.findAll();
    }

    public Optional<EstadoIncidencia> findEstadoById(Integer id) {
        return estadoIncidenciaRepository.findById(id);
    }
}
