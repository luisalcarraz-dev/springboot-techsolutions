package com.TechSolutions.Soporte.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarContrasena {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminHash   = encoder.encode("admin1234");
        String soporteHash = encoder.encode("soporte1234");
        String usuarioHash = encoder.encode("Maria1234");
        String jefeHash = encoder.encode("jefe1234");

        System.out.println("ADMIN:   " + adminHash);
        System.out.println("SOPORTE: " + soporteHash);
        System.out.println("USUARIO: " + usuarioHash);      
        System.out.println("JEFE: " + jefeHash);
    }
}