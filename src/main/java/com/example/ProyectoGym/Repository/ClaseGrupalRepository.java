package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.ClaseGrupal;
import com.example.ProyectoGym.Model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de clases grupales del gimnasio.
 * Proporciona métodos para consultar clases por estado, instructor y nombre.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface ClaseGrupalRepository extends JpaRepository<ClaseGrupal, Long> {

    /**
     * Obtiene todas las clases grupales activas.
     *
     * @return Lista de clases grupales con estado activo
     */
    List<ClaseGrupal> findByActivaTrue();

    /**
     * Busca una clase grupal por su nombre exacto.
     *
     * @param nombre Nombre de la clase a buscar
     * @return Optional conteniendo la clase si existe, empty en caso contrario
     */
    Optional<ClaseGrupal> findByNombre(String nombre);

    /**
     * Obtiene todas las clases grupales asignadas a un instructor específico.
     *
     * @param instructor El instructor del cual obtener las clases
     * @return Lista de clases grupales del instructor
     */
    List<ClaseGrupal> findByInstructor(Instructor instructor);

    /**
     * Obtiene las clases activas de un instructor específico.
     * Combina filtro por instructor y estado activo.
     *
     * @param instructor El instructor del cual obtener las clases activas
     * @return Lista de clases activas del instructor
     */
    List<ClaseGrupal> findByInstructorAndActivaTrue(Instructor instructor);

    /**
     * Cuenta el número total de clases grupales activas en el gimnasio.
     *
     * @return Cantidad de clases activas
     */
    @Query("SELECT COUNT(c) FROM ClaseGrupal c WHERE c.activa = true")
    Long countClasesActivas();
}