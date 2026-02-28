package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.*;
import com.TechSolutions.Soporte.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SoporteService {

    @Autowired private IncidenciaRepository incidenciaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TipoIncidenciaRepository tipoIncidenciaRepository;
    @Autowired private PrioridadRepository prioridadRepository;
    @Autowired private AsignacionRepository asignacionRepository;
    @Autowired private ClasificacionRepository clasificacionRepository;
    @Autowired private EstadoIncidenciaRepository estadoIncidenciaRepository;
    @Autowired private OrdenTrabajoRepository ordenTrabajoRepository;

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
                .filter(i -> i.getEstado() != null
                        && i.getFechaRegistro() != null
                        && ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre()))
                        && i.getFechaRegistro().isBefore(LocalDateTime.now().minusDays(2)))
                .count();

        long cerradosMes = todasLasIncidencias.stream()
                .filter(i -> i.getEstado() != null
                        && i.getFechaRegistro() != null
                        && "CERRADO".equals(i.getEstado().getNombre())
                        && i.getFechaRegistro().getMonth() == LocalDate.now().getMonth()
                        && i.getFechaRegistro().getYear() == LocalDate.now().getYear())
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
                .filter(i -> i.getEstado() != null && i.getFechaRegistro() != null &&
                        (
                                (("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre()))
                                        && i.getFechaRegistro().isBefore(LocalDateTime.now().minusDays(2)))
                                        ||
                                        (i.getClasificacion() != null
                                                && i.getClasificacion().getPrioridad() != null
                                                && "ALTA".equalsIgnoreCase(i.getClasificacion().getPrioridad().getNombre()))
                        ))
                .sorted((i1, i2) -> i2.getFechaRegistro().compareTo(i1.getFechaRegistro()))
                .collect(Collectors.toList());
    }

    public List<CargaTrabajoTecnicoDTO> getCargaTrabajoTecnicos() {
        List<Usuario> tecnicos = usuarioRepository.findByRol_Nombre("TECNICO");

        return tecnicos.stream().map(tecnico -> {
            List<Incidencia> incidenciasTecnico =
                    incidenciaRepository.findIncidenciasByTecnicoId(tecnico.getIdUsuario());

            long asignados = incidenciasTecnico.size();

            long enAtencion = incidenciasTecnico.stream()
                    .filter(i -> i.getEstado() != null && "EN_PROCESO".equals(i.getEstado().getNombre()))
                    .count();

            long atrasados = incidenciasTecnico.stream()
                    .filter(i -> i.getEstado() != null
                            && i.getFechaRegistro() != null
                            && ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre()))
                            && i.getFechaRegistro().isBefore(LocalDateTime.now().minusDays(2)))
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
    public Incidencia asignarYClasificarIncidencia(Integer incidenciaId, Integer tipoId, Integer prioridadId,
                                                   Integer tiempoObjetivoHoras, Integer tecnicoId) {

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
        asignacion.setFechaAsignacion(LocalDateTime.now());
        asignacion.setActiva(true);
        asignacionRepository.save(asignacion);
        incidencia.setAsignacion(asignacion);

        incidencia.setEstado(estadoEnProceso);
        return incidenciaRepository.save(incidencia);
    }

    public Map<String, Long> getResumenDiarioIncidencias(LocalDate fechaReporte) {

        LocalDateTime inicio = fechaReporte.atStartOfDay();
        LocalDateTime fin = fechaReporte.plusDays(1).atStartOfDay();

        List<Incidencia> incidenciasDelDia = incidenciaRepository.findByFechaRegistroBetween(inicio, fin);

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
                .filter(i -> i.getEstado() != null
                        && i.getFechaRegistro() != null
                        && ("ABIERTO".equals(i.getEstado().getNombre()) || "EN_PROCESO".equals(i.getEstado().getNombre()))
                        && i.getFechaRegistro().isBefore(LocalDateTime.now().minusDays(2)))
                .count());

        return resumen;
    }

    public List<Incidencia> getDetalleIncidenciasDiarias(LocalDate fechaReporte) {
        LocalDateTime inicio = fechaReporte.atStartOfDay();
        LocalDateTime fin = fechaReporte.plusDays(1).atStartOfDay();
        return incidenciaRepository.findByFechaRegistroBetween(inicio, fin);
    }

    public Map<String, List<?>> getTiempoPromedioAtencionPorDia(int numDias) {
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        for (int i = numDias - 1; i >= 0; i--) {
            LocalDate fecha = LocalDate.now().minusDays(i);
            labels.add(fecha.getDayOfWeek().name().substring(0, 3));

            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

            List<Incidencia> cerradas = incidenciaRepository.findCerradasByFechaCierreBetween(inicio, fin);

            if (cerradas.isEmpty()) {
                data.add(0.0);
                continue;
            }

            double totalHoras = 0;
            long count = 0;

            for (Incidencia inc : cerradas) {
                if (inc.getFechaCierre() != null && inc.getFechaRegistro() != null) {
                    long diffHoras = ChronoUnit.HOURS.between(inc.getFechaRegistro(), inc.getFechaCierre());
                    totalHoras += diffHoras;
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

    public Map<String, Map<String, Double>> getComparacionTiempoObjetivoVsRealPorPrioridad() {
        Map<String, Map<String, Double>> comparacion = new HashMap<>();
        List<Prioridad> todasPrioridades = prioridadRepository.findAll();

        for (Prioridad prioridad : todasPrioridades) {
            List<Incidencia> incidenciasPorPrioridad =
                    incidenciaRepository.findIncidenciasCerradasByPrioridad(prioridad.getIdPrioridad());

            double totalTiempoObjetivo = 0;
            double totalTiempoReal = 0;
            long count = 0;

            for (Incidencia inc : incidenciasPorPrioridad) {
                if (inc.getClasificacion() != null && inc.getFechaCierre() != null && inc.getFechaRegistro() != null) {
                    totalTiempoObjetivo += inc.getClasificacion().getTiempoObjetivoHoras();

                    long diffHoras = ChronoUnit.HOURS.between(inc.getFechaRegistro(), inc.getFechaCierre());
                    totalTiempoReal += diffHoras;
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
    
    @Transactional
    public void reasignarTecnico(Integer idIncidencia, Integer nuevoTecnicoId, String observacionesJefe) {

        Incidencia inc = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        if (inc.getAsignacion() == null) {
            throw new RuntimeException("La incidencia no tiene asignación para reasignar.");
        }

        Usuario nuevoTecnico = usuarioRepository.findById(nuevoTecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico no encontrado."));

        Asignacion asig = inc.getAsignacion();
        asig.setTecnico(nuevoTecnico);

        OrdenTrabajo ot = ordenTrabajoRepository
                .findByAsignacion_IdAsignacion(asig.getIdAsignacion())
                .orElse(null);

        if (ot != null && observacionesJefe != null && !observacionesJefe.trim().isEmpty()) {
            String obsActual = (ot.getObservaciones() == null) ? "" : ot.getObservaciones();
            ot.setObservaciones((obsActual + "\n[Obs. Jefe Soporte] " + observacionesJefe).trim());
            ordenTrabajoRepository.save(ot);
        }

        asignacionRepository.save(asig);
    }

    @Transactional
    public void solicitarApoyo(Integer idIncidencia) {

        Incidencia inc = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        EstadoIncidencia estado = estadoIncidenciaRepository.findByNombre("APOYO_SOLICITADO")
                .orElseThrow(() -> new RuntimeException("Estado 'APOYO_SOLICITADO' no existe."));

        inc.setEstado(estado);
        incidenciaRepository.save(inc);
    }

    @Transactional
    public void guardarObservacionesJefe(Integer idIncidencia, String observacionesJefe) {

        if (observacionesJefe == null || observacionesJefe.trim().isEmpty()) {
            return;
        }

        Incidencia inc = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada."));

        if (inc.getAsignacion() == null) {
            throw new RuntimeException("La incidencia no tiene asignación (no hay orden de trabajo asociada).");
        }

        OrdenTrabajo ot = ordenTrabajoRepository
                .findByAsignacion_IdAsignacion(inc.getAsignacion().getIdAsignacion())
                .orElseThrow(() -> new RuntimeException("No existe orden de trabajo para esta asignación."));

        String obsActual = (ot.getObservaciones() == null) ? "" : ot.getObservaciones();
        ot.setObservaciones((obsActual + "\n[Obs. Jefe Soporte] " + observacionesJefe).trim());

        ordenTrabajoRepository.save(ot);
    }
}