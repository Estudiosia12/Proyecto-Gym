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

/**
 * Repositorio para la gestión de asistencias de miembros al gimnasio.
 * Proporciona métodos para registrar entradas y salidas, consultar asistencias
 * y obtener estadísticas de uso del gimnasio.
 *
 * @author Juan Quispe, Pedro Llamas
 * @since 2025
 */

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /**
     * Busca la asistencia activa de un miembro específico.
     * Útil para verificar si un miembro ya tiene una entrada abierta antes de registrar una nueva.
     *
     * @param miembro El miembro del cual buscar la asistencia activa
     * @return Optional conteniendo la asistencia activa si existe, empty si no hay asistencia abierta
     */
    @Query("SELECT a FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraSalida IS NULL")
    Optional<Asistencia> findAsistenciaActivaByMiembro(@Param("miembro") Miembro miembro);

    /**
     * Verifica si un miembro está actualmente en el gimnasio.
     * Retorna true si existe al menos una asistencia sin salida registrada para el miembro.
     *
     * @param miembro El miembro a verificar
     * @return true si el miembro está en el gimnasio, false en caso contrario
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraSalida IS NULL")
    boolean miembroEstaEnGimnasio(@Param("miembro") Miembro miembro);

    /**
     * Obtiene todas las asistencias registradas en el día actual.
     * Las asistencias se ordenan por fecha de entrada descendente (más recientes primero).
     *
     * @return Lista de asistencias del día actual
     */
    @Query("SELECT a FROM Asistencia a WHERE CAST(a.fechaHoraEntrada AS date) = CURRENT_DATE ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findAsistenciasHoy();

    /**
     * Obtiene asistencias registradas entre dos fechas específicas.
     * Útil para generar reportes de asistencia en períodos personalizados.
     *
     * @param inicio Fecha y hora de inicio del rango
     * @param fin Fecha y hora de fin del rango
     * @return Lista de asistencias dentro del rango especificado, ordenadas por entrada descendente
     */
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHoraEntrada BETWEEN :inicio AND :fin ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findAsistenciasBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene todas las asistencias de un miembro específico.
     * Metodo derivado de Spring Data JPA, ordenadas por fecha de entrada descendente.
     *
     * @param miembro El miembro del cual obtener las asistencias
     * @return Lista de asistencias del miembro ordenadas de más reciente a más antigua
     */
    List<Asistencia> findByMiembroOrderByFechaHoraEntradaDesc(Miembro miembro);

    /**
     * Obtiene las asistencias de un miembro específico dentro de un rango de fechas.
     * Permite generar reportes de asistencia personalizados por miembro y período.
     *
     * @param miembro El miembro del cual consultar las asistencias
     * @param inicio Fecha y hora de inicio del rango
     * @param fin Fecha y hora de fin del rango
     * @return Lista de asistencias del miembro en el rango especificado
     */
    @Query("SELECT a FROM Asistencia a WHERE a.miembro = :miembro AND a.fechaHoraEntrada BETWEEN :inicio AND :fin ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findByMiembroAndFechaBetween(@Param("miembro") Miembro miembro,
                                                  @Param("inicio") LocalDateTime inicio,
                                                  @Param("fin") LocalDateTime fin);

    /**
     * Cuenta el número total de asistencias registradas en el día actual.
     * Útil para obtener estadísticas diarias del gimnasio.
     *
     * @return Número de asistencias del día
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE CAST(a.fechaHoraEntrada AS date) = CURRENT_DATE")
    long countAsistenciasHoy();

    /**
     * Cuenta el número de miembros que están actualmente en el gimnasio.
     * Se consideran miembros presentes aquellos con asistencias sin salida registrada.
     *
     * @return Número de miembros actualmente en el gimnasio
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.fechaHoraSalida IS NULL")
    long countMiembrosEnGimnasio();

    /**
     * Obtiene todas las asistencias de un mes y año específicos.
     * Permite generar reportes mensuales de asistencia.
     *
     * @param mes Número del mes (1-12)
     * @param anio Año a consultar
     * @return Lista de asistencias del mes y año especificados
     */
    @Query("SELECT a FROM Asistencia a WHERE EXTRACT(MONTH FROM a.fechaHoraEntrada) = :mes AND EXTRACT(YEAR FROM a.fechaHoraEntrada) = :anio ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findByMesAndAnio(@Param("mes") int mes, @Param("anio") int anio);

    /**
     * Cuenta las asistencias de un miembro específico en el mes actual.
     * Útil para verificar el uso del plan de membresía mensual.
     *
     * @param miembro El miembro del cual contar las asistencias
     * @return Número de asistencias del miembro en el mes actual
     */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.miembro = :miembro AND MONTH(a.fechaHoraEntrada) = MONTH(CURRENT_DATE) AND YEAR(a.fechaHoraEntrada) = YEAR(CURRENT_DATE)")
    long countAsistenciasMiembroMesActual(@Param("miembro") Miembro miembro);

    /**
     * Obtiene la lista de miembros que están actualmente en el gimnasio.
     * Retorna las asistencias sin salida registrada, ordenadas por entrada descendente.
     *
     * @return Lista de asistencias activas (miembros presentes en el gimnasio)
     */
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHoraSalida IS NULL ORDER BY a.fechaHoraEntrada DESC")
    List<Asistencia> findMiembrosEnGimnasio();
}