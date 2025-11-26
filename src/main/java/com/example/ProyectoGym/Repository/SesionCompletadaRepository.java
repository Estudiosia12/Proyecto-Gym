package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.SesionCompletada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la gestion de sesiones de entrenamiento completadas por los miembros.
 * Proporciona metodos para consultar historial, estadisticas y progreso de entrenamientos.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface SesionCompletadaRepository extends JpaRepository<SesionCompletada, Long> {

    /**
     * Busca todas las sesiones completadas de un miembro ordenadas por fecha descendente.
     * Permite visualizar el historial de entrenamientos mas recientes primero.
     *
     * @param miembro El miembro del cual obtener las sesiones
     * @return Lista de sesiones ordenadas de mas reciente a mas antigua
     */
    List<SesionCompletada> findByMiembroOrderByFechaCompletadaDesc(Miembro miembro);

    /**
     * Cuenta las sesiones completadas por un miembro en el mes actual.
     * Util para generar estadisticas mensuales y medir constancia del miembro.
     *
     * @param miembro El miembro del cual contar sesiones
     * @return Cantidad de sesiones completadas en el mes actual
     */
    @Query("SELECT COUNT(s) FROM SesionCompletada s WHERE s.miembro = :miembro " +
            "AND MONTH(s.fechaCompletada) = MONTH(CURRENT_DATE) " +
            "AND YEAR(s.fechaCompletada) = YEAR(CURRENT_DATE)")
    Long countSesionesEsteMes(@Param("miembro") Miembro miembro);

    /**
     * Cuenta el total de sesiones completadas por un miembro desde su registro.
     *
     * @param miembro El miembro del cual contar el total de sesiones
     * @return Cantidad total de sesiones completadas
     */
    Long countByMiembro(Miembro miembro);

    /**
     * Obtiene la ultima sesion completada por un miembro.
     * Util para mostrar informacion de la ultima visita o entrenamiento.
     *
     * @param miembro El miembro del cual obtener la ultima sesion
     * @return La sesion mas reciente del miembro
     */
    SesionCompletada findFirstByMiembroOrderByFechaCompletadaDesc(Miembro miembro);

    /**
     * Cuenta las sesiones completadas por un miembro en el mes anterior.
     * Permite comparar el progreso mensual del miembro.
     *
     * @param miembro El miembro del cual contar sesiones
     * @return Cantidad de sesiones completadas en el mes anterior
     */
    @Query("SELECT COUNT(s) FROM SesionCompletada s WHERE s.miembro = :miembro " +
            "AND MONTH(s.fechaCompletada) = MONTH(CURRENT_DATE) - 1 " +
            "AND YEAR(s.fechaCompletada) = YEAR(CURRENT_DATE)")
    Long countSesionesMesAnterior(@Param("miembro") Miembro miembro);

    /**
     * Cuenta las sesiones completadas en una fecha especifica.
     * Util para generar estadisticas diarias del gimnasio.
     *
     * @param fecha Fecha a consultar
     * @return Cantidad de sesiones completadas en la fecha
     */
    long countByFechaCompletada(LocalDate fecha);

    /**
     * Cuenta las sesiones completadas dentro de un rango de fechas.
     * Permite generar reportes personalizados por periodo.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Cantidad de sesiones completadas en el rango
     */
    long countByFechaCompletadaBetween(LocalDate inicio, LocalDate fin);

    /**
     * Busca todas las sesiones completadas en una fecha especifica.
     *
     * @param fecha Fecha a consultar
     * @return Lista de sesiones completadas en la fecha
     */
    List<SesionCompletada> findByFechaCompletada(LocalDate fecha);

    /**
     * Busca sesiones completadas dentro de un rango de fechas.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de sesiones completadas en el rango especificado
     */
    List<SesionCompletada> findByFechaCompletadaBetween(LocalDate inicio, LocalDate fin);
}