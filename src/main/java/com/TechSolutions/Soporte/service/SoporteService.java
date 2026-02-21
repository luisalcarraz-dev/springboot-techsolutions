// src/main/java/com/TechSolutions.Soporte/service/SoporteService.java
package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.AsignacionRepository;
import com.TechSolutions.Soporte.Repository.ClasificacionRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.Repository.OrdenTrabajoRepository;
import com.TechSolutions.Soporte.Repository.PrioridadRepository;
import com.TechSolutions.Soporte.Repository.TipoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.UsuarioRepository;
import com.TechSolutions.Soporte.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // Para calcular diferencias de tiempo
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private OrdenTrabajoRepository ordenTrabajoRepository;

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

        asignacion.setTecnico(nuevoTecnico);
        asignacion.setMotivo("Reasignación por Jefe de Soporte");
        asignacion.setFechaAsignacion(LocalDate.now());
        asignacionRepository.save(asignacion);

        return incidenciaRepository.save(incidencia);
    }

    @Transactional
    public void guardarObservacionesJefe(Integer incidenciaId, String observacionesJefe) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        System.out.println("Observaciones del Jefe de Soporte para incidencia " + incidenciaId + ": " + observacionesJefe);
    }

    @Transactional
    public void solicitarApoyo(Integer incidenciaId) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        System.out.println("Solicitud de apoyo para incidencia: " + incidenciaId);
    }

    // --- Métodos para Reporte Diario de Incidencias ---

    public Map<String, Long> getResumenDiarioIncidencias(LocalDate fechaReporte) {
        List<Incidencia> incidenciasDelDia = incidenciaRepository.findByFechaRegistro(fechaReporte);
        Map<String, Long> resumen = new HashMap<>();

        resumen.put("abiertos", incidenciasDelDia.stream()
                .filter(i -> i.getEstado() != null && "ABIERTO".equals(i.getEstado().getNombre()))
                .count());
        resumen.put("enProceso", incidenciasDelDia.stream()
                .filter(i -> i.getEstado() != null && "EN_PROCESO".equals(i.getEstado().getNombre()))
                .count());
        resumen.put("cerrados", incidenciasDelDia.stream()
                .filter(i -> i.getEstado() != null && "CERRADO".equals(i.getEstado().getNombre()))
                .count());
        resumen.put("atrasados", incidenciasDelDia.stream()
                .filter(i -> i.getEstado() != null &&
                             ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre())) &&
                             i.getFechaRegistro().isBefore(fechaReporte.minusDays(2)))
                .count());

        return resumen;
    }

    public List<Incidencia> getDetalleIncidenciasDiarias(LocalDate fechaReporte) {
        return incidenciaRepository.findByFechaRegistro(fechaReporte);
    }

    // --- Nuevos Métodos para Reporte de Tiempos de Atención ---

    /**
     * Calcula el tiempo promedio de atención para incidencias cerradas en un rango de días.
     * @param numDias Número de días hacia atrás para el cálculo.
     * @return Una lista de DTOs o un mapa de etiquetas y datos para el gráfico.
     */
    public Map<String, List<?>> getTiempoPromedioAtencionPorDia(int numDias) {
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (int i = numDias - 1; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);
            labels.add(fecha.getDayOfWeek().name().substring(0, 3)); // Ej. MON, TUE

            List<Incidencia> incidenciasCerradasDelDia = incidenciaRepository.findByFechaCierre(fecha);

            if (incidenciasCerradasDelDia.isEmpty()) {
                data.add(0.0);
                continue;
            }

            double totalHoras = 0;
            long count = 0;

            for (Incidencia incidencia : incidenciasCerradasDelDia) {
                // Asegurarse de que tiene fecha de cierre y registro, y que el estado es CERRADO
                if (incidencia.getFechaCierre() != null && incidencia.getFechaRegistro() != null &&
                    incidencia.getEstado() != null && "CERRADO".equals(incidencia.getEstado().getNombre())) {
                    
                    // Cálculo simple en días, luego se puede mejorar para horas/minutos exactos
                    long diffDays = ChronoUnit.DAYS.between(incidencia.getFechaRegistro(), incidencia.getFechaCierre());
                    totalHoras += diffDays * 24; // Convertir a horas
                    count++;
                }
            }
            data.add(count > 0 ? totalHoras / count : 0.0);
        }

        Map<String, List<?>> resultado = new HashMap<>();
        resultado.put("labels", labels);
        resultado.put("data", data);
        return resultado;
    }

    /**
     * Calcula y compara el tiempo objetivo vs real por prioridad para incidencias cerradas.
     * @return Un mapa con los tiempos objetivo y real para cada prioridad.
     */
    public Map<String, Map<String, Double>> getComparacionTiempoObjetivoVsRealPorPrioridad() {
        Map<String, Map<String, Double>> comparacion = new HashMap<>();
        List<Prioridad> todasPrioridades = prioridadRepository.findAll();

        for (Prioridad prioridad : todasPrioridades) {
            List<Incidencia> incidenciasPorPrioridad = incidenciaRepository.findIncidenciasCerradasByPrioridad(prioridad.getIdPrioridad());

            double totalTiempoObjetivo = 0;
            double totalTiempoReal = 0;
            long count = 0;

            for (Incidencia incidencia : incidenciasPorPrioridad) {
                if (incidencia.getClasificacion() != null && incidencia.getFechaCierre() != null && incidencia.getFechaRegistro() != null) {
                    totalTiempoObjetivo += incidencia.getClasificacion().getTiempoObjetivoHoras();
                    long diffDays = ChronoUnit.DAYS.between(incidencia.getFechaRegistro(), incidencia.getFechaCierre());
                    totalTiempoReal += diffDays * 24; // Convertir a horas
                    count++;
                }
            }

            Map<String, Double> tiempos = new HashMap<>();
            tiempos.put("objetivo", count > 0 ? totalTiempoObjetivo / count : 0.0);
            tiempos.put("real", count > 0 ? totalTiempoReal / count : 0.0);
            comparacion.put(prioridad.getNombre(), tiempos);
        }
        return comparacion;
    }
}
