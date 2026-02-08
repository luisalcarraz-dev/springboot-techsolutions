package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "dni", length = 12)
    private String dni;

    @Column(name = "nombres", nullable = false, length = 80)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 80)
    private String apellidos;

    @Column(name = "correo", length = 120, unique = true)
    private String correo;

    @Column(name = "username", nullable = false, length = 60, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    //Relación con Rol
    @ManyToOne
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;
}

