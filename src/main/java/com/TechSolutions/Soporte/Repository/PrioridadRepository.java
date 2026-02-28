package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrioridadRepository extends JpaRepository<Prioridad, Integer> {
}
