package com.TechSolutions.Soporte.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "id_canal", nullable = false)
    private CanalContacto canalContacto;

    @Column(name = "asunto", nullable = false, length = 120)
    private String asunto;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private EstadoIncidencia estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @OneToOne(mappedBy = "incidencia", cascade = CascadeType.ALL)
    private Clasificacion clasificacion;

    @OneToOne(mappedBy = "incidencia", cascade = CascadeType.ALL)
    private Asignacion asignacion;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL)
    private List<Historial> historial;
}