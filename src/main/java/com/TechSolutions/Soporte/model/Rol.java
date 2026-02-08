package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "rol")
@Data
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol", nullable = false)
    private Integer id_rol;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;
}
