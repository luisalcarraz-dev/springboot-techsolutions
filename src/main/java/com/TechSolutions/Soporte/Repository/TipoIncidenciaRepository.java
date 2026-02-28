package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.TipoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoIncidenciaRepository extends JpaRepository<TipoIncidencia, Integer> {
}
