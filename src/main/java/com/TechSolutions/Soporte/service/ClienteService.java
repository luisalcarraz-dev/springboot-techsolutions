package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.Repository.ActaConformidadRepository;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.ActaConformidad;
import com.TechSolutions.Soporte.model.Usuario;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClienteService {

    private final IncidenciaRepository incidenciaRepository;
    private final ActaConformidadRepository actaRepository;

    public ClienteService(IncidenciaRepository incidenciaRepository,
                          ActaConformidadRepository actaRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.actaRepository = actaRepository;
    }

    public Map<String, Long> obtenerEstadisticas(Integer idCliente) {
        List<Incidencia> tickets = incidenciaRepository.findByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente);
        
        long abiertos = tickets.stream()
                .filter(t -> "ABIERTO".equals(t.getEstado().getNombre()))
                .count();
        
        long enProceso = tickets.stream()
                .filter(t -> "EN_PROCESO".equals(t.getEstado().getNombre()))
                .count();
        
        long cerrados = tickets.stream()
                .filter(t -> "CERRADO".equals(t.getEstado().getNombre()))
                .count();
        
        // Tickets atrasados (más de 48 horas sin cerrar)
        long atrasados = tickets.stream()
                .filter(t -> !"CERRADO".equals(t.getEstado().getNombre()))
                .filter(t -> t.getFechaRegistro().isBefore(LocalDate.now().minusDays(2)))
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("abiertos", abiertos);
        stats.put("enProceso", enProceso);
        stats.put("cerrados", cerrados);
        stats.put("atrasados", atrasados);
        
        return stats;
    }

    public List<Incidencia> obtenerUltimosTickets(Integer idCliente, int limite) {
        return incidenciaRepository.findByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente)
                .stream()
                .limit(limite)
                .toList();
    }

    public Incidencia buscarTicketCliente(Integer idIncidencia, Integer idCliente) {
    	return incidenciaRepository.findByIdIncidenciaAndCliente_IdUsuario(idIncidencia, idCliente);
    }

    public void guardarConformidad(Integer idIncidencia, Integer idCliente, 
                                   Boolean conforme, String comentario) {
        Incidencia ticket = incidenciaRepository.findById(idIncidencia).orElse(null);
        if (ticket == null || !idCliente.equals(ticket.getCliente().getIdUsuario())) {
            return;
        }

        ActaConformidad acta = new ActaConformidad();
        acta.setIncidencia(ticket);
        
        Usuario cliente = new Usuario();
        cliente.setIdUsuario(idCliente);
        acta.setCliente(cliente);
        
        acta.setConforme(conforme);
        acta.setComentario(comentario);
        acta.setFechaConformidad(LocalDate.now());
        
        actaRepository.save(acta);
    }
}