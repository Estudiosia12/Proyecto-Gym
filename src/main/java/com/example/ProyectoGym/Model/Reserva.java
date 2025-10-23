package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    @ManyToOne
    @JoinColumn(name = "clase_id", nullable = false)
    private ClaseGrupal claseGrupal;

    @Column(name = "fecha_reserva")
    private LocalDateTime fechaReserva;

    @Column
    private String estado;

    // Constructores
    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.estado = "ACTIVA";
    }

    public Reserva(Miembro miembro, ClaseGrupal claseGrupal) {
        this.miembro = miembro;
        this.claseGrupal = claseGrupal;
        this.fechaReserva = LocalDateTime.now();
        this.estado = "ACTIVA";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Miembro getMiembro() {
        return miembro;
    }

    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

    public ClaseGrupal getClaseGrupal() {
        return claseGrupal;
    }

    public void setClaseGrupal(ClaseGrupal claseGrupal) {
        this.claseGrupal = claseGrupal;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
