package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "estado_incidencia")
@Data
public class EstadoIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado", nullable = false)
    private Integer idEstado;

    @Column(name = "nombre", nullable = false, length = 20, unique = true)
    private String nombre;
}
