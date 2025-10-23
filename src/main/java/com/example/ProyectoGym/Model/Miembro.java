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
}