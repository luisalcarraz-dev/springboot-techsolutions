package com.TechSolutions.Soporte.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.TechSolutions.Soporte.Repository.HistorialRepository;
import com.TechSolutions.Soporte.model.Historial;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;

@Service
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public void registrarEvento(Incidencia incidencia, Usuario usuario, String tipoEvento, String detalle) {
        Historial h = new Historial();
        h.setIncidencia(incidencia);
        h.setUsuario(usuario);
        h.setTipoEvento(tipoEvento);
        h.setDetalle(detalle);
        h.setFechaEvento(LocalDateTime.now());
        historialRepository.save(h);
    }
}