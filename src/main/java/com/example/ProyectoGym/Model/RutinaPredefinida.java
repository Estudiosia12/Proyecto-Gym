package com.example.ProyectoGym.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "rutina_predefinida")
public class RutinaPredefinida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 50)
    private String objetivo;

    @Column(nullable = false, length = 50)
    private String nivel;

    @Column
    private Integer duracion; // en minutos

    @Column(name = "frecuencia_semanal")
    private Integer frecuenciaSemanal;

    @Column
    private Boolean activo = true;

    // Constructores
    public RutinaPredefinida() {
    }

    public RutinaPredefinida(String nombre, String descripcion, String objetivo, String nivel,
                             Integer duracion, Integer frecuenciaSemanal) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.objetivo = objetivo;
        this.nivel = nivel;
        this.duracion = duracion;
        this.frecuenciaSemanal = frecuenciaSemanal;
        this.activo = true;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(String objetivo) {
        this.objetivo = objetivo;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public Integer getFrecuenciaSemanal() {
        return frecuenciaSemanal;
    }

    public void setFrecuenciaSemanal(Integer frecuenciaSemanal) {
        this.frecuenciaSemanal = frecuenciaSemanal;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
