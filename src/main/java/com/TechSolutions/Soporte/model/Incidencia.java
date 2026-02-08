package com.TechSolutions.Soporte.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "incidencia")
@Data
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_incidencia", nullable = false)
    private Integer idIncidencia;

    @Column(name = "codigo_ticket", nullable = false, length = 20, unique = true)
    private String codigoTicket;

    //Cliente (Usuario)
    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    //Canal de contacto
    @ManyToOne
    @JoinColumn(name = "id_canal", nullable = false)
    private CanalContacto canalContacto;

    @Column(name = "asunto", nullable = false, length = 120)
    private String asunto;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    //Estado de incidencia
    @ManyToOne
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoIncidencia estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;
}