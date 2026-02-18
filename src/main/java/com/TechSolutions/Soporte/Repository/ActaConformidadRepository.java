package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.ActaConformidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActaConformidadRepository extends JpaRepository<ActaConformidad, Integer> {
    Optional<ActaConformidad> findByIncidencia_IdIncidencia(Integer idIncidencia);
}