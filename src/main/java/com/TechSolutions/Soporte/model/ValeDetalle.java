package com.TechSolutions.Soporte.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vale_detalle")
@Data
public class ValeDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    @ManyToOne
    @JoinColumn(name = "id_vale", nullable = false)
    private ValeSuministros vale;

    @ManyToOne
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
