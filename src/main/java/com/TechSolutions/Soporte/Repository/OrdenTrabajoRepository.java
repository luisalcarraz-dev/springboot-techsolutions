package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.OrdenTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Integer> {
    Optional<OrdenTrabajo> findByAsignacion_IdAsignacion(Integer idAsignacion);
}
