package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "canal_contacto")
@Data
public class CanalContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal", nullable = false)
    private Integer idCanal;

    @Column(name = "nombre", nullable = false, length = 40, unique = true)
    private String nombre;
}
