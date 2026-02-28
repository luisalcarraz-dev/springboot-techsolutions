package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer> {
}
