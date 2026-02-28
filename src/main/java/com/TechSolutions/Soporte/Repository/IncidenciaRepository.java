package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Integer> {

    boolean existsByCodigoTicket(String codigoTicket);

    List<Incidencia> findByEstado_NombreOrderByIdIncidenciaDesc(String nombreEstado);
    List<Incidencia> findByCliente_IdUsuario(Integer idCliente);
    List<Incidencia> findByCliente_IdUsuarioOrderByIdIncidenciaDesc(Integer idCliente);

    Incidencia findByIdIncidenciaAndCliente_IdUsuario(Integer idIncidencia, Integer idCliente);


    @Query("SELECT i FROM Incidencia i JOIN i.asignacion a WHERE a.tecnico.idUsuario = :tecnicoId")
    List<Incidencia> findIncidenciasByTecnicoId(@Param("tecnicoId") Integer tecnicoId);


    @Query("SELECT i FROM Incidencia i WHERE i.fechaRegistro >= :inicio AND i.fechaRegistro < :fin")
    List<Incidencia> findByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio,
                                                @Param("fin") LocalDateTime fin);

    @Query("SELECT i FROM Incidencia i WHERE i.fechaCierre >= :inicio AND i.fechaCierre < :fin AND i.estado.nombre = 'CERRADO'")
    List<Incidencia> findCerradasByFechaCierreBetween(@Param("inicio") LocalDateTime inicio,
                                                      @Param("fin") LocalDateTime fin);

    @Query("SELECT i FROM Incidencia i JOIN i.clasificacion c " +
           "WHERE c.prioridad.idPrioridad = :prioridadId AND i.estado.nombre = 'CERRADO'")
    List<Incidencia> findIncidenciasCerradasByPrioridad(@Param("prioridadId") Integer prioridadId);
}