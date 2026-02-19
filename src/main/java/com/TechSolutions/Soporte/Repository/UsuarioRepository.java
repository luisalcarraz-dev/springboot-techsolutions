// src/main/java/com/TechSolutions.Soporte/Repository/UsuarioRepository.java
package com.TechSolutions.Soporte.Repository;

import com.TechSolutions.Soporte.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username); // Ya lo tenías para Login
    List<Usuario> findByRol_Nombre(String nombreRol); // Nuevo método para obtener técnicos
}
