// src/main/java/com/TechSolutions.Soporte/Repository/AsignacionRepository.java
package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Asignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AsignacionRepository extends JpaRepository<Asignacion, Integer> {
    // Métodos si los necesitas
}
