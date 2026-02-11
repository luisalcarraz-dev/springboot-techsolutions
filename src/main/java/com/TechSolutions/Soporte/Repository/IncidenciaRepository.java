package com.TechSolutions.Soporte.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TechSolutions.Soporte.model.Incidencia;

public interface IncidenciaRepository extends  JpaRepository<Incidencia, Integer>{

	boolean existsByCodigoTicket(String codigoTicket);
}
