package com.TechSolutions.Soporte.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public Incidencia registrarIncidencia(Incidencia incidencia) {

        // Generar código de ticket
        String codigo;
        do {
            codigo = "TCK-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        } while (incidenciaRepository.existsByCodigoTicket(codigo));

        incidencia.setCodigoTicket(codigo);

        // Estado inicial: ABIERTO (id = 1)
        EstadoIncidencia estado = new EstadoIncidencia();
        estado.setIdEstado(1);
        incidencia.setEstado(estado);

        // Fecha actual
        incidencia.setFechaRegistro(LocalDate.now());

        return incidenciaRepository.save(incidencia);
    }
}