package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.SesionCompletada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SesionCompletadaRepository extends JpaRepository<SesionCompletada, Long> {

    // Buscar sesiones de un miembro ordenadas por fecha descendente
    List<SesionCompletada> findByMiembroOrderByFechaCompletadaDesc(Miembro miembro);

    // Contar sesiones de un miembro en el mes actual
    @Query("SELECT COUNT(s) FROM SesionCompletada s WHERE s.miembro = :miembro " +
            "AND MONTH(s.fechaCompletada) = MONTH(CURRENT_DATE) " +
            "AND YEAR(s.fechaCompletada) = YEAR(CURRENT_DATE)")
    Long countSesionesEsteMes(@Param("miembro") Miembro miembro);

    // Contar total de sesiones de un miembro
    Long countByMiembro(Miembro miembro);

    // Buscar última sesión de un miembro
    SesionCompletada findFirstByMiembroOrderByFechaCompletadaDesc(Miembro miembro);

    // Contar sesiones del mes anterior
    @Query("SELECT COUNT(s) FROM SesionCompletada s WHERE s.miembro = :miembro " +
            "AND MONTH(s.fechaCompletada) = MONTH(CURRENT_DATE) - 1 " +
            "AND YEAR(s.fechaCompletada) = YEAR(CURRENT_DATE)")
    Long countSesionesMesAnterior(@Param("miembro") Miembro miembro);


    // Contar sesiones completadas en una fecha específica (con long minúscula)
    long countByFechaCompletada(LocalDate fecha);

    // Contar sesiones completadas entre dos fechas
    long countByFechaCompletadaBetween(LocalDate inicio, LocalDate fin);

    // Buscar sesiones por fecha
    List<SesionCompletada> findByFechaCompletada(LocalDate fecha);

    // Buscar sesiones entre fechas
    List<SesionCompletada> findByFechaCompletadaBetween(LocalDate inicio, LocalDate fin);
}
