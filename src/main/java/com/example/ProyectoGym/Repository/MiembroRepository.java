package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MiembroRepository extends JpaRepository<Miembro, Long> {

    // Buscar miembro por DNI y password
    Optional<Miembro> findByDniAndPassword(String dni, String password);

    // Buscar miembro por email
    Optional<Miembro> findByEmail(String email);

    // Buscar miembro por DNI
    Optional<Miembro> findByDni(String dni);

    // Verificar si existe miembro por email
    boolean existsByEmail(String email);

    // Verificar si existe miembro por DNI
    boolean existsByDni(String dni);

    // Obtener todos los miembros activos
    @Query("SELECT m FROM Miembro m WHERE m.activo = true")
    List<Miembro> findMiembrosActivos();

    // NUEVO: Miembros activos CON planes cargados (evita lazy loading)
    @Query("SELECT m FROM Miembro m LEFT JOIN FETCH m.planDetalle WHERE m.activo = true AND m.fechaVencimiento >= CURRENT_DATE")
    List<Miembro> findMiembrosActivosConPlan();

    // Alternativa sin @Query (hace lo mismo):
    // List<Miembro> findByActivoTrue();

    // Obtener todos los miembros inactivos
    List<Miembro> findByActivoFalse();

    // Contar miembros por plan (con @Query personalizado)
    @Query("SELECT COUNT(m) FROM Miembro m WHERE m.plan = :plan AND m.activo = true")
    Long countByPlan(@Param("plan") String plan);

    // Buscar miembros por plan
    List<Miembro> findByPlan(String plan);

    // Buscar miembros activos por plan
    List<Miembro> findByPlanAndActivoTrue(String plan);

    // Buscar miembros cuya fecha de vencimiento esté entre dos fechas
    List<Miembro> findByFechaVencimientoBetween(LocalDate inicio, LocalDate fin);

    // Buscar miembros con fecha de vencimiento antes de una fecha (vencidos)
    List<Miembro> findByFechaVencimientoBefore(LocalDate fecha);

    // Buscar miembros con fecha de vencimiento después de una fecha (vigentes)
    List<Miembro> findByFechaVencimientoAfter(LocalDate fecha);

    // Buscar miembros registrados en una fecha específica
    List<Miembro> findByFechaRegistro(LocalDate fechaRegistro);

    // Buscar miembros registrados entre dos fechas
    List<Miembro> findByFechaRegistroBetween(LocalDate inicio, LocalDate fin);
}