// src/main/java/com/TechSolutions.Soporte/service/SoporteService.java
package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.AsignacionRepository;
import com.TechSolutions.Soporte.Repository.ClasificacionRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.Repository.OrdenTrabajoRepository; // Necesario para acceder a OrdenTrabajo
import com.TechSolutions.Soporte.Repository.PrioridadRepository;
import com.TechSolutions.Soporte.Repository.TipoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.UsuarioRepository;
import com.TechSolutions.Soporte.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
public class SoporteService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TipoIncidenciaRepository tipoIncidenciaRepository;
    @Autowired
    private PrioridadRepository prioridadRepository;
    @Autowired
    private AsignacionRepository asignacionRepository;
    @Autowired
    private ClasificacionRepository clasificacionRepository;
    @Autowired
    private EstadoIncidenciaRepository estadoIncidenciaRepository;
    @Autowired
    private OrdenTrabajoRepository ordenTrabajoRepository; // Inyectar OrdenTrabajoRepository

    // Clase DTO para la carga de trabajo de técnicos
    public static class CargaTrabajoTecnicoDTO {
        private Usuario tecnico;
        private long asignados;
        private long enAtencion;
        private long atrasados;

        public CargaTrabajoTecnicoDTO(Usuario tecnico, long asignados, long enAtencion, long atrasados) {
            this.tecnico = tecnico;
            this.asignados = asignados;
            this.enAtencion = enAtencion;
            this.atrasados = atrasados;
        }

        public Usuario getTecnico() { return tecnico; }
        public long getAsignados() { return asignados; }
        public long getEnAtencion() { return enAtencion; }
        public long getAtrasados() { return atrasados; }
    }

    public List<Incidencia> findAllIncidencias() {
        return incidenciaRepository.findAll();
    }

    public Map<String, Long> calcularKpis(List<Incidencia> todasLasIncidencias) {
        Map<String, Long> kpis = new HashMap<>();

        long abiertos = todasLasIncidencias.stream()
                .filter(i -> i.getEstado() != null && "ABIERTO".equals(i.getEstado().getNombre()))
                .count();
        long enAtencion = todasLasIncidencias.stream()
                .filter(i -> i.getEstado() != null && "EN_PROCESO".equals(i.getEstado().getNombre()))
                .count();
        long atrasados = todasLasIncidencias.stream()
                .filter(i -> i.getEstado() != null &&
                             ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())) &&
                             i.getFechaRegistro().isBefore(LocalDate.now().minusDays(2)))
                .count();
        long cerradosMes = todasLasIncidencias.stream()
                .filter(i -> i.getEstado() != null && "CERRADO".equals(i.getEstado().getNombre()) &&
                             i.getFechaRegistro().getMonth() == LocalDate.now().getMonth() &&
                             i.getFechaRegistro().getYear() == LocalDate.now().getYear())
                .count();

        kpis.put("abiertos", abiertos);
        kpis.put("enAtencion", enAtencion);
        kpis.put("atrasados", atrasados);
        kpis.put("cerradosMes", cerradosMes);

        return kpis;
    }

    public List<Incidencia> getIncidenciasAtrasadasCriticas() {
        List<Incidencia> todas = incidenciaRepository.findAll();
        return todas.stream()
                .filter(i -> i.getEstado() != null &&
                             (("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())) &&
                             i.getFechaRegistro().isBefore(LocalDate.now().minusDays(2)) ||
                             (i.getClasificacion() != null && "ALTA".equalsIgnoreCase(i.getClasificacion().getPrioridad().getNombre()))
                             ))
                .sorted((i1, i2) -> i2.getFechaRegistro().compareTo(i1.getFechaRegistro()))
                .collect(Collectors.toList());
    }

    public List<CargaTrabajoTecnicoDTO> getCargaTrabajoTecnicos() {
        List<Usuario> tecnicos = usuarioRepository.findByRol_Nombre("TECNICO");

        return tecnicos.stream().map(tecnico -> {
            List<Incidencia> incidenciasTecnico = incidenciaRepository.findIncidenciasByTecnicoId(tecnico.getIdUsuario());

            long asignados = incidenciasTecnico.size();
            long enAtencion = incidenciasTecnico.stream()
                    .filter(i -> i.getEstado() != null && "EN_PROCESO".equals(i.getEstado().getNombre()))
                    .count();
            long atrasados = incidenciasTecnico.stream()
                    .filter(i -> i.getEstado() != null &&
                                 ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())) &&
                                 i.getFechaRegistro().isBefore(LocalDate.now().minusDays(2)))
                    .count();
            return new CargaTrabajoTecnicoDTO(tecnico, asignados, enAtencion, atrasados);
        }).collect(Collectors.toList());
    }

    public List<TipoIncidencia> findAllTiposIncidencia() {
        return tipoIncidenciaRepository.findAll();
    }

    public List<Prioridad> findAllPrioridades() {
        return prioridadRepository.findAll();
    }

    public List<Usuario> findAllTecnicos() {
        return usuarioRepository.findByRol_Nombre("TECNICO");
    }

    public Incidencia buscarIncidenciaPorId(Integer id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Incidencia asignarYClasificarIncidencia(Integer incidenciaId, Integer tipoId, Integer prioridadId, Integer tiempoObjetivoHoras, Integer tecnicoId) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        TipoIncidencia tipo = tipoIncidenciaRepository.findById(tipoId)
                .orElseThrow(() -> new RuntimeException("Tipo de incidencia no válido."));
        Prioridad prioridad = prioridadRepository.findById(prioridadId)
                .orElseThrow(() -> new RuntimeException("Prioridad no válida."));
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado."));
        EstadoIncidencia estadoEnProceso = estadoIncidenciaRepository.findByNombre("EN_PROCESO")
                .orElseThrow(() -> new RuntimeException("Estado 'EN_PROCESO' no encontrado."));

        Clasificacion clasificacion = incidencia.getClasificacion();
        if (clasificacion == null) {
            clasificacion = new Clasificacion();
            clasificacion.setIncidencia(incidencia);
        }
        clasificacion.setTipo(tipo);
        clasificacion.setPrioridad(prioridad);
        clasificacion.setTiempoObjetivoHoras(tiempoObjetivoHoras);
        clasificacion.setFechaClasificacion(LocalDate.now());
        clasificacionRepository.save(clasificacion);
        incidencia.setClasificacion(clasificacion);

        Asignacion asignacion = incidencia.getAsignacion();
        if (asignacion == null) {
            asignacion = new Asignacion();
            asignacion.setIncidencia(incidencia);
        }
        asignacion.setTecnico(tecnico);
        asignacion.setMotivo("Asignación por Jefe de Soporte");
        asignacion.setFechaAsignacion(LocalDate.now());
        asignacion.setActiva(true);
        asignacionRepository.save(asignacion);
        incidencia.setAsignacion(asignacion);

        incidencia.setEstado(estadoEnProceso);

        return incidenciaRepository.save(incidencia);
    }

    // --- Métodos para la Revisión de Tickets (Jefe de Soporte) ---

    /**
     * Reasigna un técnico a una incidencia existente.
     * @param incidenciaId ID de la incidencia.
     * @param nuevoTecnicoId ID del nuevo técnico a asignar.
     * @param observacionesJefe Observaciones del jefe de soporte.
     * @return La incidencia actualizada.
     */
    @Transactional
    public Incidencia reasignarTecnico(Integer incidenciaId, Integer nuevoTecnicoId, String observacionesJefe) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        Usuario nuevoTecnico = usuarioRepository.findById(nuevoTecnicoId)
                .orElseThrow(() -> new RuntimeException("Nuevo técnico no encontrado."));

        Asignacion asignacion = incidencia.getAsignacion();
        if (asignacion == null) {
            throw new RuntimeException("La incidencia no tiene una asignación activa para reasignar.");
        }

        // Actualizar la asignación existente
        asignacion.setTecnico(nuevoTecnico);
        asignacion.setMotivo("Reasignación por Jefe de Soporte"); // Puedes añadir el motivo del jefe aquí
        asignacion.setFechaAsignacion(LocalDate.now()); // Actualizar fecha de asignación
        asignacionRepository.save(asignacion);

        // Opcional: Podrías añadir un registro en el historial para la reasignación
        // historialService.registrarEvento(incidencia, "REASIGNACION", "Técnico reasignado a " + nuevoTecnico.getNombres());

        // Guardar observaciones del jefe (si aplica, podríamos necesitar una entidad para esto)
        // Por ahora, solo se reasigna. Las observaciones se manejarán por separado si no hay un campo directo.

        return incidenciaRepository.save(incidencia); // Guardar la incidencia (aunque la asignación ya está actualizada)
    }

    /**
     * Guarda observaciones del Jefe de Soporte para una incidencia.
     * @param incidenciaId ID de la incidencia.
     * @param observacionesJefe Las observaciones a guardar.
     */
    @Transactional
    public void guardarObservacionesJefe(Integer incidenciaId, String observacionesJefe) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        // Aquí necesitaríamos un campo en la entidad Incidencia o una entidad Historial/Comentario
        // para guardar las observaciones del jefe.
        // Por ahora, solo lo imprimiremos o lo dejaremos como un TODO.
        System.out.println("Observaciones del Jefe de Soporte para incidencia " + incidenciaId + ": " + observacionesJefe);
        // TODO: Implementar lógica para guardar las observaciones del jefe.
        // Podrías añadir un campo 'observacionesJefe' en la entidad Incidencia o crear una entidad 'ComentarioJefe'.
    }

    /**
     * Lógica para "Solicitar Apoyo". Esto podría implicar notificaciones o escalamientos.
     * @param incidenciaId ID de la incidencia.
     */
    @Transactional
    public void solicitarApoyo(Integer incidenciaId) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        // TODO: Implementar lógica de notificación o escalamiento.
        // Esto podría ser:
        // - Enviar un correo electrónico a otro jefe o a un grupo de soporte.
        // - Cambiar el estado de la incidencia a "ESCALADA".
        // - Crear una entrada en la entidad Escalamiento.
        System.out.println("Solicitud de apoyo para incidencia: " + incidenciaId);
        // Por ejemplo, cambiar el estado a un estado de escalamiento si existe
        // Optional<EstadoIncidencia> estadoEscalado = estadoIncidenciaRepository.findByNombre("ESCALADA");
        // if (estadoEscalado.isPresent()) {
        //     incidencia.setEstado(estadoEscalado.get());
        //     incidenciaRepository.save(incidencia);
        // }
    }
}
