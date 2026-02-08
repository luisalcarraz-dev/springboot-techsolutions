package com.TechSolutions.Soporte.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "escalamiento")
@Data
public class Escalamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_escalamiento")
    private Integer idEscalamiento;

    @ManyToOne
    @JoinColumn(name = "id_incidencia", nullable = false)
    private Incidencia incidencia;

    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @ManyToOne
    @JoinColumn(name = "id_notificado_a", nullable = false)
    private Usuario notificadoA;

    @Column(name = "fecha_escalamiento", nullable = false)
    private LocalDate fechaEscalamiento;

    @Column(name = "atendido", nullable = false)
    private Boolean atendido;

    private LocalDate fechaAtendido;
}
