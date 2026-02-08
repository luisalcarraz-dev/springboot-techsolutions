package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "clasificacion")
@Data
public class Clasificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_clasificacion", nullable = false)
    private Integer idClasificacion;

    // 🔗 Incidencia (1 a 1)
    @OneToOne
    @JoinColumn(name = "id_incidencia", nullable = false, unique = true)
    private Incidencia incidencia;

    // 🔗 Tipo de incidencia
    @ManyToOne
    @JoinColumn(name = "id_tipo", nullable = false)
    private TipoIncidencia tipo;

    // 🔗 Prioridad
    @ManyToOne
    @JoinColumn(name = "id_prioridad", nullable = false)
    private Prioridad prioridad;

    @Column(name = "tiempo_objetivo_horas", nullable = false)
    private Integer tiempoObjetivoHoras;

    @Column(name = "fecha_clasificacion", nullable = false)
    private LocalDate fechaClasificacion;
}
