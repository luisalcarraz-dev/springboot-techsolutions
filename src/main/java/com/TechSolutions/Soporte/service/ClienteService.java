// src/main/java/com/TechSolutions.Soporte/service/ClienteService.java
package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.ActaConformidadRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.ActaConformidad;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
    private ActaConformidadRepository actaConformidadRepository; // Inyectar el repositorio
    
    @Autowired
    private EstadoIncidenciaRepository estadoIncidenciaRepository; // Inyectar el repositorio

    // Método para obtener estadísticas de incidencias por estado para un cliente
    public Map<String, Long> obtenerEstadisticas(Integer idCliente) {
        List<Incidencia> incidenciasCliente = incidenciaRepository.findByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente);
        Map<String, Long> estadisticas = new HashMap<>();

        long abiertos = incidenciasCliente.stream()
                .filter(i -> "ABIERTO".equals(i.getEstado().getNombre()))
                .count();
        long enProceso = incidenciasCliente.stream()
                .filter(i -> "EN_PROCESO".equals(i.getEstado().getNombre()))
                .count();
        long cerrados = incidenciasCliente.stream()
                .filter(i -> "CERRADO".equals(i.getEstado().getNombre()))
                .count();
        
        // Para 'Atrasados', tu HTML usa una lógica basada en fechaRegistro.
        // Aquí podemos replicar una lógica similar o ajustarla según tu definición de "atrasado".
        // Por ejemplo, si una incidencia abierta o en proceso tiene más de 2 días.
        long atrasados = incidenciasCliente.stream()
                .filter(i -> ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())) && 
                             i.getFechaRegistro().isBefore(LocalDate.now().minusDays(2)))
                .count();

        estadisticas.put("abiertos", abiertos);
        estadisticas.put("enProceso", enProceso);
        estadisticas.put("cerrados", cerrados);
        estadisticas.put("atrasados", atrasados); // Asegúrate de que esta clave coincida con tu HTML
        
        return estadisticas;
    }

    // Método para obtener los últimos N tickets de un cliente
    public List<Incidencia> obtenerUltimosTickets(Integer idCliente, int limite) {
        // Tu IncidenciaRepository ya tiene findByCliente_IdUsuarioOrderByIdIncidenciaDesc
        // Podemos limitar el resultado si es necesario, o simplemente devolver todos y que la vista los maneje.
        // Para un límite, podríamos usar Pageable si el repositorio extiende PagingAndSortingRepository.
        // Por ahora, devolveremos todos y la vista puede mostrar los primeros N.
        return incidenciaRepository.findByCliente_IdUsuarioOrderByIdIncidenciaDesc(idCliente)
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    // Método para buscar un ticket específico de un cliente
    public Incidencia buscarTicketCliente(Integer idIncidencia, Integer idCliente) {
        return incidenciaRepository.findByIdIncidenciaAndCliente_IdUsuario(idIncidencia, idCliente);
    }

    
    @Transactional // Asegura que ambas operaciones (guardar acta y actualizar incidencia) sean atómicas
    public void guardarConformidad(Integer idIncidencia, Integer idCliente, Boolean conforme, String comentario) {
        // 1. Buscar la incidencia
        Incidencia incidencia = incidenciaRepository.findByIdIncidenciaAndCliente_IdUsuario(idIncidencia, idCliente);
        if (incidencia == null) {
            throw new RuntimeException("Incidencia no encontrada o no pertenece al cliente.");
        }
        
        // 2. Verificar si ya existe un acta para esta incidencia (para evitar duplicados)
        Optional<ActaConformidad> actaExistente = actaConformidadRepository.findByIncidencia_IdIncidencia(idIncidencia);
        ActaConformidad acta = actaExistente.orElseGet(ActaConformidad::new); // Si existe, la usa; si no, crea una nueva

        // 3. Crear/Actualizar la entidad ActaConformidad
        acta.setIncidencia(incidencia);
        acta.setCliente(incidencia.getCliente()); // El cliente de la incidencia es quien da la conformidad
        acta.setConforme(conforme);
        acta.setComentario(comentario);
        acta.setFechaConformidad(LocalDate.now());
        
        actaConformidadRepository.save(acta); // Guardar el acta

        // 4. Actualizar el estado de la incidencia según la conformidad
        if (conforme) {
            // Si el cliente da conformidad, el estado ya debería ser CERRADO (por la validación del controller)
            // No es necesario cambiarlo, pero podríamos añadir una verificación si se desea.
            // Por ejemplo, si se quiere un estado "CONFORMADO" distinto de "CERRADO".
            // Para este caso, asumimos que si llega aquí y es conforme, el estado CERRADO es final.
        } else {
            // Si el cliente NO da conformidad, cambiar el estado a "EN_PROCESO" o a un nuevo estado como "RECHAZADO"
            // Asumo que existe un estado "EN_PROCESO" con el nombre "EN_PROCESO" en tu DB.
            Optional<EstadoIncidencia> estadoEnProceso = estadoIncidenciaRepository.findByNombre("EN_PROCESO");
            if (estadoEnProceso.isPresent()) {
                incidencia.setEstado(estadoEnProceso.get());
                incidenciaRepository.save(incidencia); // Guardar la incidencia con el nuevo estado
            } else {
                throw new RuntimeException("Estado 'EN_PROCESO' no encontrado en la base de datos.");
            }
        }
    }
}