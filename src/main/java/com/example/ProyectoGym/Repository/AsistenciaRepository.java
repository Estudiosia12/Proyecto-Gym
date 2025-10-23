package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Asistencia;
import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    // Buscar asistencia activa (sin salida) de un miembro
    @Query("SELECT a FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraSalida IS NULL")
    Optional<Asistencia> findAsistenciaActivaByMiembro(@Param("miembro") Miembro miembro);

    // Verificar si un miembro está actualmente en el gimnasio
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraSalida IS NULL")
    boolean miembroEstaEnGimnasio(@Param("miembro") Miembro miembro);

    // Obtener todas las asistencias del día de hoy
    @Query("SELECT a FROM Asistencia a WHERE CAST(a.fechaHoraEntrada AS date) = CURRENT_DATE ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findAsistenciasHoy();

    // Obtener asistencias entre dos fechas
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHoraEntrada BETWEEN :inicio AND :fin ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findAsistenciasBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Obtener asistencias de un miembro específico
    List<Asistencia> findByMiembroOrderByFechaHoraEntradaDesc(Miembro miembro);

    // Obtener asistencias de un miembro en un rango de fechas
    @Query("SELECT a FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraEntrada BETWEEN :inicio AND :fin ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findByMiembroAndFechaBetween(@Param("miembro") Miembro miembro,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fin") LocalDateTime fin);

    // Contar asistencias de hoy
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE CAST(a.fechaHoraEntrada AS date) = CURRENT_DATE")
    long countAsistenciasHoy();

    // Contar miembros actualmente en el gimnasio
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.fechaHoraSalida IS NULL")
    long countMiembrosEnGimnasio();

    // Obtener asistencias por mes
    @Query("SELECT a FROM Asistencia a WHERE EXTRACT(MONTH FROM a.fechaHoraEntrada) = :mes AND EXTRACT(YEAR FROM a.fechaHoraEntrada) = :anio ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findByMesAndAnio(@Param("mes") int mes, @Param("anio") int anio);

    // Contar asistencias de un miembro en el mes actual
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.miembro = :miembro AND MONTH(a.fechaHoraEntrada) = MONTH(CURRENT_DATE) AND YEAR(a.fechaHoraEntrada) = YEAR(CURRENT_DATE)")
    long countAsistenciasMiembroMesActual(@Param("miembro") Miembro miembro);

    // Obtener miembros actualmente en el gimnasio
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHoraSalida IS NULL ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findMiembrosEnGimnasio();
}