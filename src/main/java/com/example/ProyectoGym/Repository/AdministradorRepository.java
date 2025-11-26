
package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la gestión de administradores del sistema.
 * Proporciona métodos para autenticación y validación de credenciales únicas.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    /**
     * Busca un administrador por su nombre de usuario.
     * Utilizado para el proceso de autenticación.
     *
     * @param usuario Nombre de usuario del administrador
     * @return Optional conteniendo el administrador si existe, empty en caso contrario
     */
    Optional<Administrador> findByUsuario(String usuario);

    /**
     * Busca un administrador por su dirección de correo electrónico.
     *
     * @param email Correo electrónico del administrador
     * @return Optional conteniendo el administrador si existe, empty en caso contrario
     */
    Optional<Administrador> findByEmail(String email);

    /**
     * Verifica si existe un administrador con el nombre de usuario especificado.
     * Útil para validar unicidad al crear o actualizar administradores.
     *
     * @param usuario Nombre de usuario a verificar
     * @return true si el usuario ya existe, false en caso contrario
     */
    boolean existsByUsuario(String usuario);

    /**
     * Verifica si existe un administrador con el correo electrónico especificado.
     * Útil para validar unicidad del email al crear o actualizar administradores.
     *
     * @param email Correo electrónico a verificar
     * @return true si el email ya existe, false en caso contrario
     */
    boolean existsByEmail(String email);
}
