package com.TechSolutions.Soporte.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.TechSolutions.Soporte.model.Incidencia;

import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    boolean existsByCodigoTicket(String codigoTicket);

    List<Incidencia> findByEstado_NombreOrderByIdIncidenciaDesc(String nombreEstado);
    
 // ✅ CORREGIDO: Usa idIncidencia en lugar de id
    List<Incidencia> findByCliente_IdUsuarioOrderByIdIncidenciaDesc(Integer idCliente);

    // ✅ CORREGIDO: Usa idIncidencia en lugar de id
    Incidencia findByIdIncidenciaAndCliente_IdUsuario(Integer idIncidencia, Integer idCliente);
}

