package com.TechSolutions.Soporte.model;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "asignacion")
@Data
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @ManyToOne
    @JoinColumn(name = "id_incidencia", nullable = false)
    private Incidencia incidencia;

    @ManyToOne
    @JoinColumn(name = "id_tecnico", nullable = false)
    private Usuario tecnico;

    @Column(name = "motivo", length = 200)
    private String motivo;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

    @Column(name = "activa", nullable = false)
    private Boolean activa;
}

