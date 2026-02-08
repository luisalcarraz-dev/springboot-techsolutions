package com.TechSolutions.Soporte.model;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name = "vale_suministros")
@Data
public class ValeSuministros {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vale")
    private Integer idVale;

    @ManyToOne
    @JoinColumn(name = "id_orden", nullable = false)
    private OrdenTrabajo ordenTrabajo;

    @Column(name = "fecha_vale", nullable = false)
    private LocalDate fechaVale;

    @Column(name = "evidencia_url", length = 400)
    private String evidenciaUrl;
}
