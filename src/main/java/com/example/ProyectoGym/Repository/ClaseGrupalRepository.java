package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.ClaseGrupal;
import com.example.ProyectoGym.Model.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaseGrupalRepository extends JpaRepository<ClaseGrupal, Long> {

    // Buscar todas las clases activas (CORREGIDO: activo → activa)
    List<ClaseGrupal> findByActivaTrue();

    // Buscar clase por nombre
    Optional<ClaseGrupal> findByNombre(String nombre);

    // Buscar clases por instructor
    List<ClaseGrupal> findByInstructor(Instructor instructor);

    // Buscar clases activas por instructor (CORREGIDO: activo → activa)
    List<ClaseGrupal> findByInstructorAndActivaTrue(Instructor instructor);

    // Contar clases activas (usando @Query personalizado porque el campo es 'activa')
    @Query("SELECT COUNT(c) FROM ClaseGrupal c WHERE c.activa = true")
    Long countClasesActivas();
}