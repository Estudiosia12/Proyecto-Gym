package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de planes de membresía del gimnasio.
 * Proporciona métodos para consultar planes por nombre, estado y beneficios incluidos.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    /**
     * Busca un plan de membresía por su nombre exacto.
     *
     * @param nombre Nombre del plan ("Básico", "Premium")
     * @return Optional conteniendo el plan si existe, empty en caso contrario
     */
    Optional<Plan> findByNombre(String nombre);

    /**
     * Obtiene todos los planes de membresía con estado activo.
     * Solo se muestran planes disponibles para nuevas suscripciones.
     *
     * @return Lista de planes activos
     */
    List<Plan> findByActivoTrue();

    /**
     * Busca planes que incluyen acceso a clases grupales.
     *
     * @return Lista de planes con acceso a clases incluido
     */
    List<Plan> findByAccesoClasesTrue();

    /**
     * Busca planes que incluyen asesoría personalizada con instructores.
     *
     * @return Lista de planes con asesoría personalizada incluida
     */
    List<Plan> findByAsesoriaPersonalizadaTrue();

    /**
     * Verifica si existe un plan con el nombre especificado.
     * Útil para validar unicidad al crear o actualizar planes.
     *
     * @param nombre Nombre del plan a verificar
     * @return true si el nombre ya existe, false en caso contrario
     */
    boolean existsByNombre(String nombre);
}