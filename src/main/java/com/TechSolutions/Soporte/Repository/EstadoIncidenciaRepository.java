// src/main/java/com/TechSolutions.Soporte/Repository/EstadoIncidenciaRepository.java
package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.EstadoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoIncidenciaRepository extends JpaRepository<EstadoIncidencia, Integer> {
    Optional<EstadoIncidencia> findByNombre(String nombre);
}
