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
                .filter(i -> i.getEstado() != null && "CERRADO".equals(i.getEstado().getNombre()) && i.getFechaRegistro().isEqual(LocalDate.now()))
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
        // 1. Validar la incidencia y la asignación
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        if (incidencia.getAsignacion() == null || !incidencia.getAsignacion().getTecnico().getIdUsuario().equals(idTecnico)) {
            throw new RuntimeException("No tiene permisos para modificar esta incidencia.");
        }

        // 2. Obtener o crear la Orden de Trabajo
        OrdenTrabajo ordenExistente = ordenTrabajoRepository.findByAsignacion_IdAsignacion(incidencia.getAsignacion().getIdAsignacion()).orElse(new OrdenTrabajo());

        // Mapear los campos del formulario a la Orden de Trabajo
        ordenExistente.setAsignacion(incidencia.getAsignacion()); // Asegurar la relación
        ordenExistente.setActividades(ordenTrabajo.getActividades());
        ordenExistente.setHerramientas(ordenTrabajo.getHerramientas());
        ordenExistente.setObservaciones(ordenTrabajo.getObservaciones());
        // Solo actualizar fechaInicio si es una nueva orden de trabajo
        if (ordenExistente.getIdOrden() == null) {
            ordenExistente.setFechaInicio(LocalDate.now());
        }

        // 3. Guardar la Orden de Trabajo
        ordenTrabajoRepository.save(ordenExistente);

        // 4. Actualizar el estado de la incidencia
        Optional<EstadoIncidencia> nuevoEstado = estadoIncidenciaRepository.findById(nuevoEstadoId);
        if (nuevoEstado.isPresent()) {
            incidencia.setEstado(nuevoEstado.get());
        } else {
            throw new RuntimeException("Estado de incidencia no válido.");
        }
        
        // Lógica adicional si se presiona "Solicitar Cierre"
        if (solicitarCierre) {
            // Si el técnico selecciona "CERRADO" y pulsa "Solicitar Cierre", la incidencia se cerrará.
            // Podrías añadir lógica aquí para enviar notificaciones o cambiar el estado a "PENDIENTE_APROBACION_CIERRE"
            if ("CERRADO".equals(nuevoEstado.get().getNombre())) {
                ordenExistente.setFechaFin(LocalDate.now()); // Si se cierra, registra la fecha de fin
                ordenTrabajoRepository.save(ordenExistente); // Guardar la orden de trabajo actualizada con fecha de fin
            }
        }

        incidenciaRepository.save(incidencia); // Guardar la incidencia con el nuevo estado
    }
    
    // --- NUEVO MÉTODO: Solicitar cierre de incidencia ---
    @Transactional
    public Incidencia solicitarCierreIncidencia(Integer idIncidencia, Integer idTecnico) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        // Verificar que el técnico está asignado a esta incidencia
        if (incidencia.getAsignacion() == null || !incidencia.getAsignacion().getTecnico().getIdUsuario().equals(idTecnico)) {
            throw new RuntimeException("El técnico no está asignado a esta incidencia.");
        }

        // Obtener el estado 'PENDIENTE_CONFORMIDAD_CLIENTE'
        EstadoIncidencia estadoPendienteConformidad = estadoIncidenciaRepository.findByNombre("PENDIENTE_CONFORMIDAD_CLIENTE")
                .orElseThrow(() -> new RuntimeException("Estado 'PENDIENTE_CONFORMIDAD_CLIENTE' no encontrado."));

        // Actualizar el estado de la incidencia
        incidencia.setEstado(estadoPendienteConformidad);
        // Opcional: Podrías registrar la fecha de solicitud de cierre en fechaCierre,
        // y luego la fecha final de cierre cuando el cliente dé conformidad.
        // incidencia.setFechaCierre(LocalDate.now()); // O considera un nuevo campo fechaSolicitudCierre

        return incidenciaRepository.save(incidencia);
    }
}
