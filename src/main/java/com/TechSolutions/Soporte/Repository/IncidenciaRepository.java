// src/main/java/com/TechSolutions/Soporte/Repository/IncidenciaRepository.java
package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    boolean existsByCodigoTicket(String codigoTicket);

    List<Incidencia> findByEstado_NombreOrderByIdIncidenciaDesc(String nombreEstado);
    
    List<Incidencia> findByCliente_IdUsuarioOrderByIdIncidenciaDesc(Integer idCliente);

    Incidencia findByIdIncidenciaAndCliente_IdUsuario(Integer idIncidencia, Integer idCliente);
    // Nuevo método para obtener incidencias asignadas a un técnico
    // Mantenemos el IdUsuario como Integer
    @Query("SELECT i FROM Incidencia i JOIN i.asignacion a WHERE a.tecnico.idUsuario = :tecnicoId") // Asegúrate que 'idUsuario' es el nombre del campo ID en tu entidad Usuario
    List<Incidencia> findIncidenciasByTecnicoId(@Param("tecnicoId") Integer tecnicoId); // Cambiado a Integer
}