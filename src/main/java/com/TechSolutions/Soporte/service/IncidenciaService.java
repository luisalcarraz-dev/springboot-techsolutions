package com.TechSolutions.Soporte.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.CanalContacto;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    public Incidencia buscarPorId(Integer id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    public Incidencia registrarIncidencia(Incidencia incidencia) {

        //  Canal por defecto si el formulario no lo envía
        // Requiere que exista canal_contacto con id=1 (WEB)
        if (incidencia.getCanalContacto() == null) {
            CanalContacto canal = new CanalContacto();
            canal.setIdCanal(1);
            incidencia.setCanalContacto(canal);
        }

        // Generar código de ticket único
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
