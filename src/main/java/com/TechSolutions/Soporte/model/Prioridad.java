package com.TechSolutions.Soporte.model;



import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "prioridad")
@Data
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prioridad", nullable = false)
    private Integer idPrioridad;

    @Column(name = "nombre", nullable = false, length = 30, unique = true)
    private String nombre;

    @Column(name = "orden_prioridad", nullable = false)
    private Integer ordenPrioridad;
}

