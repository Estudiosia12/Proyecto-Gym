package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.AsignacionRutina;
import com.example.ProyectoGym.Model.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionRutinaRepository extends JpaRepository<AsignacionRutina, Long> {

    // Buscar asignación activa de un miembro
    Optional<AsignacionRutina> findByMiembroAndActivoTrue(Miembro miembro);

    // Verificar si un miembro tiene una rutina asignada
    boolean existsByMiembroAndActivoTrue(Miembro miembro);
}
