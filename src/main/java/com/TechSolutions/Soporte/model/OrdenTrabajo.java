package com.TechSolutions.Soporte.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orden_trabajo")
@Data
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Integer idOrden;

    @OneToOne
    @JoinColumn(name = "id_asignacion", nullable = false)
    private Asignacion asignacion;

    @Column(name = "actividades", nullable = false, columnDefinition = "TEXT")
    private String actividades;

    @Column(name = "herramientas", columnDefinition = "TEXT")
    private String herramientas;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "recomendaciones", columnDefinition = "TEXT")
    private String recomendaciones;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
}
