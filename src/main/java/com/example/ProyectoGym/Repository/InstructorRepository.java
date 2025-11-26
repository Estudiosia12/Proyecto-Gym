package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de instructores del gimnasio.
 * Proporciona métodos para consultar instructores por DNI, email, especialidad y estado.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    /**
     * Busca un instructor por su número de DNI.
     *
     * @param dni Documento Nacional de Identidad del instructor
     * @return Optional conteniendo el instructor si existe, empty en caso contrario
     */
    Optional<Instructor> findByDni(String dni);

    /**
     * Busca un instructor por su dirección de correo electrónico.
     *
     * @param email Correo electrónico del instructor
     * @return Optional conteniendo el instructor si existe, empty en caso contrario
     */
    Optional<Instructor> findByEmail(String email);

    /**
     * Obtiene todos los instructores con estado activo.
     *
     * @return Lista de instructores activos
     */
    List<Instructor> findByActivoTrue();

    /**
     * Busca instructores por su área de especialidad.
     *
     * @param especialidad Especialidad a buscar ("Yoga", "CrossFit", "Spinning")
     * @return Lista de instructores con la especialidad indicada
     */
    List<Instructor> findByEspecialidad(String especialidad);

    /**
     * Busca instructores activos filtrados por especialidad.
     * Combina filtro por especialidad y estado activo.
     *
     * @param especialidad Especialidad a buscar
     * @return Lista de instructores activos con la especialidad indicada
     */
    List<Instructor> findByEspecialidadAndActivoTrue(String especialidad);

    /**
     * Verifica si existe un instructor con el DNI especificado.
     * Útil para validar unicidad al crear o actualizar instructores.
     *
     * @param dni DNI a verificar
     * @return true si el DNI ya existe, false en caso contrario
     */
    boolean existsByDni(String dni);

    /**
     * Verifica si existe un instructor con el correo electrónico especificado.
     * Útil para validar unicidad del email al crear o actualizar instructores.
     *
     * @param email Correo electrónico a verificar
     * @return true si el email ya existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}