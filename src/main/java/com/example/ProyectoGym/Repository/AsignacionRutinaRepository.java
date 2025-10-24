package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.AsignacionRutina;
import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de asignaciones de rutinas a miembros.
 * Permite consultar y verificar las rutinas activas asignadas a los miembros del gimnasio.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface AsignacionRutinaRepository extends JpaRepository<AsignacionRutina, Long> {

    /**
     * Busca la asignación de rutina activa de un miembro específico.
     * Un miembro solo puede tener una rutina activa a la vez.
     *
     * @param miembro El miembro del cual buscar la rutina activa
     * @return Optional conteniendo la asignación activa si existe, empty en caso contrario
     */
    Optional<AsignacionRutina> findByMiembroAndActivoTrue(Miembro miembro);

    /**
     * Verifica si un miembro tiene una rutina asignada y activa.
     *
     * @param miembro El miembro a verificar
     * @return true si el miembro tiene una rutina activa, false en caso contrario
     */
    boolean existsByMiembroAndActivoTrue(Miembro miembro);
}
