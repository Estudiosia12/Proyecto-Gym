package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "planes")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "acceso_clases")
    private Boolean accesoClases = false;

    @Column(name = "asesoria_personalizada")
    private Boolean asesoriaPersonalizada = false;

    @Column(nullable = false)
    private Boolean activo = true;

    // Constructores
    public Plan() {
        this.activo = true;
    }

    public Plan(String nombre, BigDecimal precio, String descripcion, Boolean accesoClases, Boolean asesoriaPersonalizada) {
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.accesoClases = accesoClases;
        this.asesoriaPersonalizada = asesoriaPersonalizada;
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

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getAccesoClases() {
        return accesoClases;
    }

    public void setAccesoClases(Boolean accesoClases) {
        this.accesoClases = accesoClases;
    }

    public Boolean getAsesoriaPersonalizada() {
        return asesoriaPersonalizada;
    }

    public void setAsesoriaPersonalizada(Boolean asesoriaPersonalizada) {
        this.asesoriaPersonalizada = asesoriaPersonalizada;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
