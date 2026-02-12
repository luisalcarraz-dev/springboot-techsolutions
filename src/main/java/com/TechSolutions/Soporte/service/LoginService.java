package com.TechSolutions.Soporte.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.TechSolutions.Soporte.Repository.LoginRepository;
import com.TechSolutions.Soporte.model.Usuario;

@Service
public class LoginService {

    @Autowired
    private LoginRepository loginRepository;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public Usuario validarLogin(String username, String password) {

        Usuario usuario = loginRepository.findByUsername(username);

        if (usuario != null && encoder.matches(password, usuario.getPasswordHash())) {

            return usuario;
        }

        return null;
    }
}
