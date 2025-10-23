package com.example.ProyectoGym.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.Duration;

@Entity
@Table(name = "asistencias")
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "miembro_id", nullable = false)
    private Miembro miembro;

    @Column(name = "fecha_hora_entrada", nullable = false)
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    @Column(name = "duracion_minutos")
    private Integer duracionMinutos;

    // Constructores
    public Asistencia() {
        this.fechaHoraEntrada = LocalDateTime.now();
    }

    public Asistencia(Miembro miembro) {
        this.miembro = miembro;
        this.fechaHoraEntrada = LocalDateTime.now();
    }

    // Metodo para registrar salida y calcular duración
    public void registrarSalida() {
        this.fechaHoraSalida = LocalDateTime.now();
        if (this.fechaHoraEntrada != null) {
            Duration duracion = Duration.between(this.fechaHoraEntrada, this.fechaHoraSalida);
            this.duracionMinutos = (int) duracion.toMinutes();
        }
    }

    // Verificar si el miembro sigue en el gimnasio
    public boolean estaEnGimnasio() {
        return this.fechaHoraSalida == null;
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

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) {
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
        // Recalcular duración cuando se actualiza la salida
        if (this.fechaHoraEntrada != null && fechaHoraSalida != null) {
            Duration duracion = Duration.between(this.fechaHoraEntrada, fechaHoraSalida);
            this.duracionMinutos = (int) duracion.toMinutes();
        }
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}
