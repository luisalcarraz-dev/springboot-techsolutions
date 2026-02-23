package com.TechSolutions.Soporte.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarContrasena {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String adminHash   = encoder.encode("admin1234");
        String tecnicoHash = encoder.encode("soporte1234");
        String tecnico1Hash = encoder.encode("2soporte1234");
        String tecnico2Hash = encoder.encode("3soporte1234");
        String usuarioHash = encoder.encode("Maria1234");
        String usuario1Hash = encoder.encode("Kevin1234");
        String usuario2Hash = encoder.encode("Angel1234");
        String jefeHash = encoder.encode("jefe1234");

        System.out.println("ADMIN:   " + adminHash);
        System.out.println("TECNICO: " + tecnicoHash);
        System.out.println("TECNICO3: " + tecnico1Hash);
        System.out.println("TECNICO4: " + tecnico2Hash);
        System.out.println("USUARIO: " + usuarioHash); 
        System.out.println("USUARIO1: " + usuario1Hash); 
        System.out.println("USUARIO2: " + usuario2Hash); 
        System.out.println("JEFE: " + jefeHash);
    }
}