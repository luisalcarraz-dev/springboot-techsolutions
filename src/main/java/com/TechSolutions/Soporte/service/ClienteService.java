// src/main/java/com/TechSolutions.Soporte/service/ClienteService.java
package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.OrdenTrabajoRepository;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.OrdenTrabajo;
import com.TechSolutions.Soporte.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private EstadoIncidenciaRepository estadoIncidenciaRepository;
    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository;

    public List<Incidencia> getIncidenciasByClienteId(Integer idCliente) {
        return incidenciaRepository.findByCliente_IdUsuario(idCliente);
    }

    public Incidencia buscarIncidenciaPorId(Integer id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    public OrdenTrabajo obtenerOrdenTrabajoPorAsignacionId(Integer idAsignacion) {
        return ordenTrabajoRepository.findByAsignacion_IdAsignacion(idAsignacion).orElse(null);
    }

    @Transactional
    public Incidencia procesarConformidadCliente(Integer idIncidencia, Integer idCliente, String accion, String comentarioCliente) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        if (!incidencia.getCliente().getIdUsuario().equals(idCliente)) {
            throw new RuntimeException("Acceso denegado. El ticket no pertenece a este cliente.");
        }

        if (!"PENDIENTE_CONFORMIDAD_CLIENTE".equals(incidencia.getEstado().getNombre())) {
            throw new RuntimeException("La incidencia no está en estado de espera de conformidad.");
        }

        if ("conforme".equals(accion)) {
            EstadoIncidencia estadoCerrado = estadoIncidenciaRepository.findByNombre("CERRADO")
                    .orElseThrow(() -> new RuntimeException("Estado 'CERRADO' no encontrado."));
            incidencia.setEstado(estadoCerrado);
            incidencia.setFechaCierre(LocalDateTime.now());
            System.out.println("Comentario del cliente: " + comentarioCliente);

        } else if ("noConforme".equals(accion)) {
            EstadoIncidencia estadoEnProceso = estadoIncidenciaRepository.findByNombre("EN_PROCESO")
                    .orElseThrow(() -> new RuntimeException("Estado 'EN_PROCESO' no encontrado."));
            incidencia.setEstado(estadoEnProceso);
            System.out.println("Cliente NO CONFORME. Comentario: " + comentarioCliente);

        } else {
            throw new IllegalArgumentException("Acción de conformidad no válida: " + accion);
        }

        return incidenciaRepository.save(incidencia);
    }

    // --- NUEVOS MÉTODOS PARA EL DASHBOARD DEL CLIENTE ---

    /**
     * Obtiene estadísticas para el dashboard del cliente.
     * @param idCliente ID del cliente.
     * @return Mapa con conteos de incidencias por estado.
     */
    public Map<String, Long> obtenerEstadisticas(Integer idCliente) {
        List<Incidencia> incidenciasCliente = incidenciaRepository.findByCliente_IdUsuario(idCliente);
        Map<String, Long> estadisticas = new HashMap<>();

        estadisticas.put("total", (long) incidenciasCliente.size());
        estadisticas.put("abiertas", incidenciasCliente.stream()
                .filter(i -> i.getEstado() != null && "ABIERTO".equals(i.getEstado().getNombre()))
                .count());
        estadisticas.put("enProceso", incidenciasCliente.stream()
                .filter(i -> i.getEstado() != null && "EN_PROCESO".equals(i.getEstado().getNombre()))
                .count());
        estadisticas.put("pendientesConformidad", incidenciasCliente.stream()
                .filter(i -> i.getEstado() != null && "PENDIENTE_CONFORMIDAD_CLIENTE".equals(i.getEstado().getNombre()))
                .count());
        estadisticas.put("cerradas", incidenciasCliente.stream()
                .filter(i -> i.getEstado() != null && "CERRADO".equals(i.getEstado().getNombre()))
                .count());
        // Puedes añadir más estadísticas si lo necesitas

        return estadisticas;
    }

    /**
     * Obtiene los últimos N tickets del cliente.
     * @param idCliente ID del cliente.
     * @param limit Cantidad máxima de tickets a devolver.
     * @return Lista de las últimas incidencias del cliente.
     */
    public List<Incidencia> obtenerUltimosTickets(Integer idCliente, int limit) {
        // Asumo que IncidenciaRepository tiene un método para esto,
        // o puedes obtener todas y luego limitar/ordenar.
        // Por ejemplo, si tienes un método en el repo:
        // return incidenciaRepository.findTopNByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente, limit);
        // Si no, puedes hacer esto:
        return incidenciaRepository.findByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Busca un ticket específico para un cliente, asegurándose de que le pertenezca.
     * @param idTicket ID del ticket.
     * @param idCliente ID del cliente.
     * @return La incidencia si pertenece al cliente, de lo contrario null.
     */
    public Incidencia buscarTicketCliente(Integer idTicket, Integer idCliente) {
        Optional<Incidencia> incidenciaOptional = incidenciaRepository.findById(idTicket);
        if (incidenciaOptional.isPresent()) {
            Incidencia incidencia = incidenciaOptional.get();
            if (incidencia.getCliente().getIdUsuario().equals(idCliente)) {
                return incidencia;
            }
        }
        return null;
    }
}
