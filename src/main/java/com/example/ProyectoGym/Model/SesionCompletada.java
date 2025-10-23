package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "sesion_completada")
public class SesionCompletada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asignacion_rutina_id", nullable = false)
    private AsignacionRutina asignacionRutina;

    @ManyToOne
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    @Column(name = "fecha_completada")
    private LocalDate fechaCompletada;

    @Column(length = 500)
    private String observaciones;

    // Constructores
    public SesionCompletada() {
        this.fechaCompletada = LocalDate.now();
    }

    public SesionCompletada(AsignacionRutina asignacionRutina, Miembro miembro, String observaciones) {
        this.asignacionRutina = asignacionRutina;
        this.miembro = miembro;
        this.observaciones = observaciones;
        this.fechaCompletada = LocalDate.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AsignacionRutina getAsignacionRutina() {
        return asignacionRutina;
    }

    public void setAsignacionRutina(AsignacionRutina asignacionRutina) {
        this.asignacionRutina = asignacionRutina;
    }

    public Miembro getMiembro() {
        return miembro;
    }

    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

    public LocalDate getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDate fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
