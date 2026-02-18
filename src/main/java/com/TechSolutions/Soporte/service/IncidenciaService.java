package com.TechSolutions.Soporte.service;

import com.TechSolutions.Soporte.Repository.ActaConformidadRepository;
import com.TechSolutions.Soporte.Repository.CanalContactoRepository;
import com.TechSolutions.Soporte.Repository.EstadoIncidenciaRepository;
import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.ActaConformidad;
import com.TechSolutions.Soporte.model.CanalContacto;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario; // Aunque no se usa directamente en este servicio, es parte del modelo
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final EstadoIncidenciaRepository estadoIncidenciaRepository;
    private final CanalContactoRepository canalContactoRepository;
    private final ActaConformidadRepository actaConformidadRepository;

    // Constructor para inyección de dependencias
    @Autowired
    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             EstadoIncidenciaRepository estadoIncidenciaRepository,
                             CanalContactoRepository canalContactoRepository,
                             ActaConformidadRepository actaConformidadRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.estadoIncidenciaRepository = estadoIncidenciaRepository;
        this.canalContactoRepository = canalContactoRepository;
        this.actaConformidadRepository = actaConformidadRepository;
    }

    /**
     * Busca una incidencia por su ID.
     * @param id El ID de la incidencia.
     * @return La incidencia encontrada o null si no existe.
     */
    public Incidencia buscarPorId(Integer id) {
        return incidenciaRepository.findById(id).orElse(null);
    }

    /**
     * Registra una nueva incidencia en el sistema.
     * Asigna un código de ticket único, estado inicial (ABIERTO), canal de contacto (WEB) y fecha de registro.
     * @param incidencia La incidencia a registrar. El cliente debe estar ya asignado.
     * @return La incidencia guardada con sus datos generados.
     */
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

        // Asignar estado inicial: ABIERTO (ID 1)
        Optional<EstadoIncidencia> estadoAbierto = estadoIncidenciaRepository.findById(1);
        if (estadoAbierto.isPresent()) {
            incidencia.setEstado(estadoAbierto.get());
        } else {
            throw new RuntimeException("Estado 'ABIERTO' (ID 1) no encontrado en la base de datos. Por favor, asegúrese de que existe.");
        }

        // Asignar canal de contacto: WEB (ID 1)
        Optional<CanalContacto> canalWeb = canalContactoRepository.findById(1);
        if (canalWeb.isPresent()) {
            incidencia.setCanalContacto(canalWeb.get());
        } else {
            throw new RuntimeException("Canal de Contacto 'WEB' (ID 1) no encontrado en la base de datos. Por favor, asegúrese de que existe.");
        }

        // Asignar fecha de registro actual
        incidencia.setFechaRegistro(LocalDate.now());

        return incidenciaRepository.save(incidencia);
    }

    /**
     * Guarda el acta de conformidad de un cliente para una incidencia.
     * Si el cliente no da conformidad, el estado de la incidencia se cambia a "EN_PROCESO".
     * @param idIncidencia El ID de la incidencia.
     * @param idCliente El ID del cliente que da la conformidad.
     * @param conforme Indica si el cliente está conforme (true) o no (false).
     * @param comentario Comentario opcional del cliente.
     */
    @Transactional
    public void guardarConformidad(Integer idIncidencia, Integer idCliente, Boolean conforme, String comentario) {
        // 1. Buscar la incidencia y verificar que pertenece al cliente
        Incidencia incidencia = incidenciaRepository.findByIdIncidenciaAndCliente_IdUsuario(idIncidencia, idCliente);
        if (incidencia == null) {
            throw new RuntimeException("Incidencia no encontrada o no pertenece al cliente.");
        }

        // 2. Crear o actualizar el ActaConformidad
        // Busca si ya existe un acta para esta incidencia para evitar duplicados
        Optional<ActaConformidad> actaExistente = actaConformidadRepository.findByIncidencia_IdIncidencia(idIncidencia);
        ActaConformidad acta = actaExistente.orElseGet(ActaConformidad::new); // Si existe, la usa; si no, crea una nueva

        acta.setIncidencia(incidencia);
        acta.setCliente(incidencia.getCliente()); // El cliente de la incidencia es quien da la conformidad
        acta.setConforme(conforme);
        acta.setComentario(comentario);
        acta.setFechaConformidad(LocalDate.now());

        actaConformidadRepository.save(acta); // Guardar el acta de conformidad

        // 3. Actualizar el estado de la incidencia si el cliente NO da conformidad
        if (!conforme) {
            Optional<EstadoIncidencia> estadoEnProceso = estadoIncidenciaRepository.findByNombre("EN_PROCESO");
            if (estadoEnProceso.isPresent()) {
                incidencia.setEstado(estadoEnProceso.get());
                incidenciaRepository.save(incidencia); // Guardar la incidencia con el nuevo estado
            } else {
                throw new RuntimeException("Estado 'EN_PROCESO' no encontrado en la base de datos. Por favor, asegúrese de que existe.");
            }
        }
        // Si el cliente está conforme, asumimos que el estado de la incidencia ya es "CERRADO"
        // (esto se valida en el ClienteController antes de llamar a este método).
    }

    /**
     * Obtiene una lista de incidencias asignadas a un técnico específico.
     * @param tecnicoId El ID del técnico.
     * @return Una lista de incidencias asignadas al técnico.
     */
    public List<Incidencia> getIncidenciasAsignadasATecnico(Integer tecnicoId) {
        return incidenciaRepository.findIncidenciasByTecnicoId(tecnicoId);
    }
}