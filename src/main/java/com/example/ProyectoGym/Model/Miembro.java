package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "miembros")
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @Column(length = 15)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fecha_nacimiento;

    @Column(name = "fecha_registro")
    private LocalDate fechaRegistro;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento; // NUEVO

    @Column(length = 20)
    private String plan;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan planDetalle;

    @Column(nullable = false)
    private Boolean activo = true;

    // Constructores
    public Miembro() {
        this.fechaRegistro = LocalDate.now();
        this.fechaVencimiento = LocalDate.now().plusMonths(1); // Por defecto: 1 mes
        this.activo = true;
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
        this.fechaVencimiento = LocalDate.now().plusMonths(1); // Calcular 1 mes desde registro
        this.activo = true;
    }

    // Metodo para calcular y actualizar fecha de vencimiento
    public void renovarMembresia(int meses) {
        if (this.fechaVencimiento == null || this.fechaVencimiento.isBefore(LocalDate.now())) {
            // Si está vencida, empezar desde hoy
            this.fechaVencimiento = LocalDate.now().plusMonths(meses);
        } else {
            // Si aún está activa, sumar desde la fecha de vencimiento actual
            this.fechaVencimiento = this.fechaVencimiento.plusMonths(meses);
        }
        this.activo = true;
    }

    // Metodo para verificar si la membresía está vencida
    public boolean estaVencida() {
        if (this.fechaVencimiento == null) {
            return true;
        }
        return LocalDate.now().isAfter(this.fechaVencimiento);
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

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Plan getPlanDetalle() {
        return planDetalle;
    }

    public void setPlanDetalle(Plan planDetalle) {
        this.planDetalle = planDetalle;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
