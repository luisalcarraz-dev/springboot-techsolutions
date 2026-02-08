package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tipo_incidencia")
@Data
public class TipoIncidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo", nullable = false)
    private Integer idTipo;

    @Column(name = "nombre", nullable = false, length = 60, unique = true)
    private String nombre;
}
