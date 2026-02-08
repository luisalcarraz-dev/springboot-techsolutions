package com.TechSolutions.Soporte.model;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "material")
@Data
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Integer idMaterial;

    @Column(name = "nombre", nullable = false, unique = true, length = 120)
    private String nombre;
}
