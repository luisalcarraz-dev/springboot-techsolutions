package com.TechSolutions.Soporte.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "acta_conformidad")
@Data
public class ActaConformidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acta")
    private Integer idActa;

    @OneToOne
    @JoinColumn(name = "id_incidencia", nullable = false, unique = true)
    private Incidencia incidencia;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @Column(name = "conforme", nullable = false)
    private Boolean conforme;

    @Column(name = "comentario", length = 300)
    private String comentario;

    @Column(name = "fecha_conformidad", nullable = false)
    private LocalDate fechaConformidad;
}

