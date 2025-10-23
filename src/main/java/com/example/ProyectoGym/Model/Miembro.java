package com.example.ProyectoGym.Model;
import jakarta.persistence.*;
import java.time.LocalDate;
public class Miembro {
    private Long id_miembro;
    private String nombre;
    private String email;
    private String password;
    private String dni;
    private String telefono;
    private LocalDate fecha_nacimiento;
    private LocalDate fechaRegistro;
    private String plan;
    private Boolean activo = true;

    public Miembro() {
        this.fechaRegistro = LocalDate.now();
    }

    public Miembro(String nombre, String email, String password, String dni, String telefono,
                   LocalDate fecha_nacimiento, String plan) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.dni = dni;
        this.telefono = telefono;
        this.fecha_nacimiento = fecha_nacimiento;
        this.plan = plan;
        this.fechaRegistro = LocalDate.now();
        this.activo = true;
    }

    public Long getId_miembro() {
        return id_miembro;
    }

    public void setId_miembro(Long id_miembro) {
        this.id_miembro = id_miembro;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public void setFecha_nacimiento(LocalDate fecha_nacimiento) {
        this.fecha_nacimiento = fecha_nacimiento;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

}
