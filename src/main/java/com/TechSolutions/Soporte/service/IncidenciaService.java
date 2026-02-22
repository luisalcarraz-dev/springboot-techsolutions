package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.ActaConformidadRepository;
import com.TechSolutions.Soporte.Repository.CanalContactoRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.ActaConformidad;
import com.TechSolutions.Soporte.model.CanalContacto;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;
    private final CanalContactoRepository canalContactoRepository;
    private final ActaConformidadRepository actaConformidadRepository;

    // ✅ NUEVO
    private final HistorialService historialService;

    @Autowired
    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             EstadoIncidenciaRepository estadoIncidenciaRepository,
                             CanalContactoRepository canalContactoRepository,
                             ActaConformidadRepository actaConformidadRepository,
                             HistorialService historialService) {
        this.incidenciaRepository = incidenciaRepository;
        this.estadoIncidenciaRepository = estadoIncidenciaRepository;
        this.canalContactoRepository = canalContactoRepository;
        this.actaConformidadRepository = actaConformidadRepository;
        this.historialService = historialService;
    }

    public Incidencia buscarPorId(Integer id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Incidencia registrarIncidencia(Incidencia incidencia) {

        // Generar código de ticket único
        String codigo;
        do {
            codigo = "TCK-" + UUID.randomUUID()
                    .toString()
                    .substring(0, 8)
                    .toUpperCase();
        } while (incidenciaRepository.existsByCodigoTicket(codigo));

        incidencia.setCodigoTicket(codigo);

        // Estado inicial: ABIERTO (ID 1)
        Optional<EstadoIncidencia> estadoAbierto = estadoIncidenciaRepository.findById(1);
        if (estadoAbierto.isPresent()) {
            incidencia.setEstado(estadoAbierto.get());
        } else {
            throw new RuntimeException("Estado 'ABIERTO' (ID 1) no encontrado en la base de datos.");
        }

        // Canal de contacto: WEB (ID 1)
        Optional<CanalContacto> canalWeb = canalContactoRepository.findById(1);
        if (canalWeb.isPresent()) {
            incidencia.setCanalContacto(canalWeb.get());
        } else {
            throw new RuntimeException("Canal de Contacto 'WEB' (ID 1) no encontrado en la base de datos.");
        }

        // Fecha actual
        incidencia.setFechaRegistro(LocalDateTime.now());

        // ✅ Guardar incidencia
        Incidencia guardada = incidenciaRepository.save(incidencia);

        // ✅ EVENTO HISTORIAL: REGISTRO
        historialService.registrarEvento(
                guardada,
                guardada.getCliente(),
                "REGISTRO",
                "Ticket registrado por el cliente."
        );

        return guardada;
    }

    @Transactional
    public void guardarConformidad(Integer idIncidencia, Integer idCliente, Boolean conforme, String comentario) {

        Incidencia incidencia = incidenciaRepository.findByIdIncidenciaAndCliente_IdUsuario(idIncidencia, idCliente);
        if (incidencia == null) {
            throw new RuntimeException("Incidencia no encontrada o no pertenece al cliente.");
        }

        Optional<ActaConformidad> actaExistente = actaConformidadRepository.findByIncidencia_IdIncidencia(idIncidencia);
        ActaConformidad acta = actaExistente.orElseGet(ActaConformidad::new);

        acta.setIncidencia(incidencia);
        acta.setCliente(incidencia.getCliente());
        acta.setConforme(conforme);
        acta.setComentario(comentario);
        acta.setFechaConformidad(LocalDateTime.now());

        actaConformidadRepository.save(acta);

        if (!conforme) {
            Optional<EstadoIncidencia> estadoEnProceso = estadoIncidenciaRepository.findByNombre("EN_PROCESO");
            if (estadoEnProceso.isPresent()) {
                incidencia.setEstado(estadoEnProceso.get());
                incidenciaRepository.save(incidencia);
            } else {
                throw new RuntimeException("Estado 'EN_PROCESO' no encontrado en la base de datos.");
            }
        }
    }

    public List<Incidencia> getIncidenciasAsignadasATecnico(Integer tecnicoId) {
        return incidenciaRepository.findIncidenciasByTecnicoId(tecnicoId);
    }
}