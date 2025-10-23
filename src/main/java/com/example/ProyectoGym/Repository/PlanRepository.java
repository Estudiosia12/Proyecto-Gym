package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    // Buscar plan por nombre
    Optional<Plan> findByNombre(String nombre);

    // Obtener todos los planes activos
    List<Plan> findByActivoTrue();

    // Buscar planes con acceso a clases
    List<Plan> findByAccesoClasesTrue();

    // Buscar planes con asesoría personalizada
    List<Plan> findByAsesoriaPersonalizadaTrue();

    // Verificar si existe plan por nombre
    boolean existsByNombre(String nombre);
}
