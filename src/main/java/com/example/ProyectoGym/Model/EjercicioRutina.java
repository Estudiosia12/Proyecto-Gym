package com.example.ProyectoGym.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "ejercicio_rutina")
public class EjercicioRutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rutina_predefinida_id", nullable = false)
    private RutinaPredefinida rutinaPredefinida;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column
    private Integer series;

    @Column
    private Integer repeticiones;

    @Column
    private Integer descanso; // en segundos

    @Column
    private Integer orden; // para ordenar los ejercicios

    @Column(length = 500)
    private String instrucciones;

    // Constructores
    public EjercicioRutina() {
    }

    public EjercicioRutina(RutinaPredefinida rutinaPredefinida, String nombre, Integer series,
                           Integer repeticiones, Integer descanso, Integer orden, String instrucciones) {
        this.rutinaPredefinida = rutinaPredefinida;
        this.nombre = nombre;
        this.series = series;
        this.repeticiones = repeticiones;
        this.descanso = descanso;
        this.orden = orden;
        this.instrucciones = instrucciones;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RutinaPredefinida getRutinaPredefinida() {
        return rutinaPredefinida;
    }

    public void setRutinaPredefinida(RutinaPredefinida rutinaPredefinida) {
        this.rutinaPredefinida = rutinaPredefinida;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(Integer series) {
        this.series = series;
    }

    public Integer getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(Integer repeticiones) {
        this.repeticiones = repeticiones;
    }

    public Integer getDescanso() {
        return descanso;
    }

    public void setDescanso(Integer descanso) {
        this.descanso = descanso;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getInstrucciones() {
        return instrucciones;
    }

    public void setInstrucciones(String instrucciones) {
        this.instrucciones = instrucciones;
    }
}
