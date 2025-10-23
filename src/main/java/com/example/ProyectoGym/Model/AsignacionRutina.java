package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "asignacion_rutina")
public class AsignacionRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    @ManyToOne
    @JoinColumn(name = "rutina_predefinida_id", nullable = false)
    private RutinaPredefinida rutinaPredefinida;

    @Column(name = "fecha_asignacion")
    private LocalDate fechaAsignacion;

    @Column(name = "objetivo_seleccionado", length = 50)
    private String objetivoSeleccionado;

    @Column(name = "nivel_seleccionado", length = 50)
    private String nivelSeleccionado;

    @Column
    private Boolean activo = true;

    // Constructores
    public AsignacionRutina() {
        this.fechaAsignacion = LocalDate.now();
    }

    public AsignacionRutina(Miembro miembro, RutinaPredefinida rutinaPredefinida,
                            String objetivoSeleccionado, String nivelSeleccionado) {
        this.miembro = miembro;
        this.rutinaPredefinida = rutinaPredefinida;
        this.objetivoSeleccionado = objetivoSeleccionado;
        this.nivelSeleccionado = nivelSeleccionado;
        this.fechaAsignacion = LocalDate.now();
        this.activo = true;
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

    public RutinaPredefinida getRutinaPredefinida() {
        return rutinaPredefinida;
    }

    public void setRutinaPredefinida(RutinaPredefinida rutinaPredefinida) {
        this.rutinaPredefinida = rutinaPredefinida;
    }

    public LocalDate getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDate fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public String getObjetivoSeleccionado() {
        return objetivoSeleccionado;
    }

    public void setObjetivoSeleccionado(String objetivoSeleccionado) {
        this.objetivoSeleccionado = objetivoSeleccionado;
    }

    public String getNivelSeleccionado() {
        return nivelSeleccionado;
    }

    public void setNivelSeleccionado(String nivelSeleccionado) {
        this.nivelSeleccionado = nivelSeleccionado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}