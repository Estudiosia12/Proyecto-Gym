package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MiembroRepository extends JpaRepository<Miembro,Long> {

    Optional<Miembro> findByDniAndPassword(String dni, String password);

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    Optional<Miembro> findByDni(String dni);

    Optional<Miembro> findByEmail(String email);

    @Query("SELECT m FROM Miembro m WHERE m.activo = true")
    java.util.List<Miembro> findMiembrosActivos();

    @Query("SELECT COUNT(m) FROM Miembro m WHERE m.plan = :plan AND m.activo = true")
    Long countByPlan(@Param("plan") String plan);

}
