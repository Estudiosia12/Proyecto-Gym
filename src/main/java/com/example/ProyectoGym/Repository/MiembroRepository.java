package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de miembros del gimnasio.
 * Proporciona métodos para autenticación, consultas por estado, plan y fechas de vencimiento.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface MiembroRepository extends JpaRepository<Miembro, Long> {

    /**
     * Busca un miembro por DNI y contraseña.
     * Utilizado para el proceso de autenticación de miembros.
     *
     * @param dni DNI del miembro
     * @param password Contraseña del miembro
     * @return Optional conteniendo el miembro si las credenciales son correctas, empty en caso contrario
     */
    Optional<Miembro> findByDniAndPassword(String dni, String password);

    /**
     * Busca un miembro por su dirección de correo electrónico.
     *
     * @param email Correo electrónico del miembro
     * @return Optional conteniendo el miembro si existe, empty en caso contrario
     */
    Optional<Miembro> findByEmail(String email);

    /**
     * Busca un miembro por su número de DNI.
     *
     * @param dni Documento Nacional de Identidad del miembro
     * @return Optional conteniendo el miembro si existe, empty en caso contrario
     */
    Optional<Miembro> findByDni(String dni);

    /**
     * Verifica si existe un miembro con el correo electrónico especificado.
     * Útil para validar unicidad del email al registrar miembros.
     *
     * @param email Correo electrónico a verificar
     * @return true si el email ya existe, false en caso contrario
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un miembro con el DNI especificado.
     * Útil para validar unicidad al registrar miembros.
     *
     * @param dni DNI a verificar
     * @return true si el DNI ya existe, false en caso contrario
     */
    boolean existsByDni(String dni);

    /**
     * Obtiene todos los miembros con estado activo.
     *
     * @return Lista de miembros activos
     */
    @Query("SELECT m FROM Miembro m WHERE m.activo = true")
    List<Miembro> findMiembrosActivos();

    /**
     * Obtiene miembros activos con sus planes cargados mediante FETCH JOIN.
     * Evita el problema de lazy loading al cargar la relación con planDetalle.
     * Solo incluye miembros cuya membresía no ha vencido.
     *
     * @return Lista de miembros activos con plan vigente y sus detalles de plan cargados
     */
    @Query("SELECT m FROM Miembro m LEFT JOIN FETCH m.planDetalle WHERE m.activo = true AND m.fechaVencimiento >= CURRENT_DATE")
    List<Miembro> findMiembrosActivosConPlan();

    /**
     * Obtiene todos los miembros con estado inactivo.
     *
     * @return Lista de miembros inactivos
     */
    List<Miembro> findByActivoFalse();

    /**
     * Cuenta el número de miembros activos suscritos a un plan específico.
     *
     * @param plan Nombre del plan a contar
     * @return Cantidad de miembros activos con el plan especificado
     */
    @Query("SELECT COUNT(m) FROM Miembro m WHERE m.plan = :plan AND m.activo = true")
    Long countByPlan(@Param("plan") String plan);

    /**
     * Busca miembros por tipo de plan de membresía.
     *
     * @param plan Nombre del plan (Baisco o Premium)
     * @return Lista de miembros con el plan especificado
     */
    List<Miembro> findByPlan(String plan);

    /**
     * Busca miembros activos filtrados por plan de membresía.
     *
     * @param plan Nombre del plan
     * @return Lista de miembros activos con el plan especificado
     */
    List<Miembro> findByPlanAndActivoTrue(String plan);

    /**
     * Busca miembros cuya fecha de vencimiento se encuentre dentro de un rango específico.
     * Útil para identificar membresías próximas a vencer.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de miembros con vencimiento en el rango especificado
     */
    List<Miembro> findByFechaVencimientoBetween(LocalDate inicio, LocalDate fin);

    /**
     * Busca miembros cuya membresía ya ha vencido.
     *
     * @param fecha Fecha de referencia para comparar vencimientos
     * @return Lista de miembros con membresía vencida antes de la fecha indicada
     */
    List<Miembro> findByFechaVencimientoBefore(LocalDate fecha);

    /**
     * Busca miembros con membresía vigente después de una fecha específica.
     *
     * @param fecha Fecha de referencia
     * @return Lista de miembros con membresía vigente después de la fecha
     */
    List<Miembro> findByFechaVencimientoAfter(LocalDate fecha);

    /**
     * Busca miembros registrados en una fecha específica.
     *
     * @param fechaRegistro Fecha de registro a buscar
     * @return Lista de miembros registrados en la fecha especificada
     */
    List<Miembro> findByFechaRegistro(LocalDate fechaRegistro);

    /**
     * Busca miembros registrados dentro de un rango de fechas.
     * Útil para generar reportes de nuevos registros por período.
     *
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de miembros registrados en el rango de fechas
     */
    List<Miembro> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin);
}