package com.TechSolutions.Soporte.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.TechSolutions.Soporte.model.Usuario;

public interface LoginRepository extends JpaRepository<Usuario, Integer> {
	
	Usuario findByUsername(String username);

}
