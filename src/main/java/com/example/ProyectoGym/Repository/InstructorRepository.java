package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    // Buscar instructor por DNI
    Optional<Instructor> findByDni(String dni);

    // Buscar instructor por email
    Optional<Instructor> findByEmail(String email);

    // Obtener todos los instructores activos
    List<Instructor> findByActivoTrue();

    // Buscar instructores por especialidad
    List<Instructor> findByEspecialidad(String especialidad);

    // Buscar instructores activos por especialidad
    List<Instructor> findByEspecialidadAndActivoTrue(String especialidad);

    // Verificar si existe instructor por DNI
    boolean existsByDni(String dni);

    // Verificar si existe instructor por email
    boolean existsByEmail(String email);
}
