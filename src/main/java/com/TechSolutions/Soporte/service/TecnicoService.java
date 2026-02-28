package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.AsignacionRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.Repository.OrdenTrabajoRepository;
import com.TechSolutions.Soporte.model.Asignacion;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.OrdenTrabajo;
import com.TechSolutions.Soporte.model.Usuario; // Aunque no se usa directamente en este servicio, es parte del modelo
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
public class TecnicoService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository;
    @Autowired
    private AsignacionRepository asignacionRepository;
    @Autowired
    private EstadoIncidenciaRepository estadoIncidenciaRepository;

    /**
     * Obtiene las incidencias asignadas a un técnico.
     * @param tecnicoId El ID del técnico.
     * @return Una lista de incidencias.
     */
    public List<Incidencia> getIncidenciasAsignadas(Integer tecnicoId) {
        return incidenciaRepository.findIncidenciasByTecnicoId(tecnicoId);
    }

    /**
     * Calcula estadísticas para el dashboard del técnico.
     * @param incidencias Lista de incidencias del técnico.
     * @return Un mapa con las estadísticas (ej. "totalAsignadas", "pendientes", "cerradasHoy").
     */
    public Map<String, Long> obtenerEstadisticasDashboard(List<Incidencia> incidencias) {
        Map<String, Long> estadisticas = new HashMap<>();

        long totalAsignadas = incidencias.size();

        long pendientes = incidencias.stream()
                .filter(i -> i.getEstado() != null && ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())))
                .count();

        long cerradasHoy = incidencias.stream()
                .filter(i -> i.getEstado() != null && "CERRADO".equals(i.getEstado().getNombre()) && i.getFechaRegistro().isEqual(LocalDateTime.now()))
                .count();

        estadisticas.put("totalAsignadas", totalAsignadas);
        estadisticas.put("pendientes", pendientes);
        estadisticas.put("cerradasHoy", cerradasHoy);

        return estadisticas;
    }

    /**
     * Busca una incidencia por su ID.
     * @param idIncidencia El ID de la incidencia.
     * @return La incidencia encontrada o null si no existe.
     */
    public Incidencia buscarIncidenciaPorId(Integer idIncidencia) {
        return incidenciaRepository.findById(idIncidencia).orElse(null);
    }

    /**
     * Obtiene la Orden de Trabajo asociada a una Asignación.
     * @param idAsignacion El ID de la asignación.
     * @return La OrdenTrabajo o null si no existe.
     */
    public OrdenTrabajo obtenerOrdenTrabajoPorAsignacionId(Integer idAsignacion) {
        return ordenTrabajoRepository.findByAsignacion_IdAsignacion(idAsignacion).orElse(null);
    }

    /**
     * Guarda o actualiza la Orden de Trabajo y actualiza el estado de la Incidencia.
     * @param idIncidencia El ID de la incidencia.
     * @param idTecnico El ID del técnico logueado.
     * @param ordenTrabajo Los datos de la OrdenTrabajo del formulario.
     * @param nuevoEstadoId El ID del nuevo estado seleccionado para la incidencia.
     * @param solicitarCierre Si el botón de "Solicitar Cierre" fue presionado.
     */
    @Transactional
    public void guardarOrdenTrabajoYActualizarIncidencia(Integer idIncidencia, Integer idTecnico, OrdenTrabajo ordenTrabajo, Integer nuevoEstadoId, boolean solicitarCierre) {
        
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        if (incidencia.getAsignacion() == null || !incidencia.getAsignacion().getTecnico().getIdUsuario().equals(idTecnico)) {
            throw new RuntimeException("No tiene permisos para modificar esta incidencia.");
        }


        OrdenTrabajo ordenExistente = ordenTrabajoRepository.findByAsignacion_IdAsignacion(incidencia.getAsignacion().getIdAsignacion()).orElse(new OrdenTrabajo());

        ordenExistente.setAsignacion(incidencia.getAsignacion()); 
        ordenExistente.setActividades(ordenTrabajo.getActividades());
        ordenExistente.setHerramientas(ordenTrabajo.getHerramientas());
        ordenExistente.setObservaciones(ordenTrabajo.getObservaciones());
        if (ordenExistente.getIdOrden() == null) {
            ordenExistente.setFechaInicio(LocalDateTime.now());
        }

        ordenTrabajoRepository.save(ordenExistente);
        Optional<EstadoIncidencia> nuevoEstado = estadoIncidenciaRepository.findById(nuevoEstadoId);
        if (nuevoEstado.isPresent()) {
            incidencia.setEstado(nuevoEstado.get());
        } else {
            throw new RuntimeException("Estado de incidencia no válido.");
        }
        if (solicitarCierre) {
              if ("CERRADO".equals(nuevoEstado.get().getNombre())) {
                ordenExistente.setFechaFin(LocalDateTime.now()); 
                ordenTrabajoRepository.save(ordenExistente); 
            }
        }

        incidenciaRepository.save(incidencia); 
    }
    
    @Transactional
    public Incidencia solicitarCierreIncidencia(Integer idIncidencia, Integer idTecnico) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        
        if (incidencia.getAsignacion() == null || !incidencia.getAsignacion().getTecnico().getIdUsuario().equals(idTecnico)) {
            throw new RuntimeException("El técnico no está asignado a esta incidencia.");
        }

        
        EstadoIncidencia estadoPendienteConformidad = estadoIncidenciaRepository.findByNombre("PENDIENTE_CONFORMIDAD_CLIENTE")
                .orElseThrow(() -> new RuntimeException("Estado 'PENDIENTE_CONFORMIDAD_CLIENTE' no encontrado."));
       
        incidencia.setEstado(estadoPendienteConformidad);
        return incidenciaRepository.save(incidencia);
    }
}
