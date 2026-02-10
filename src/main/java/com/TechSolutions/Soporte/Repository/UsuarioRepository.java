package com.TechSolutions.Soporte.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TechSolutions.Soporte.model.Usuario;

public interface UsuarioRepository extends  JpaRepository<UsuarioRepository, Integer>{

	Optional<Usuario> findByUsername(String username);
	
}
