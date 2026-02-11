package com.TechSolutions.Soporte.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TechSolutions.Soporte.model.EstadoIncidencia;

public interface EstadoIncidenciaRepository extends  JpaRepository<EstadoIncidencia, Integer> {

}
