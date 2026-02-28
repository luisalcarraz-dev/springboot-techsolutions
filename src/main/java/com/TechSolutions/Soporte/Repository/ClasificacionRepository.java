package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Clasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClasificacionRepository extends JpaRepository<Clasificacion, Integer> {
}
