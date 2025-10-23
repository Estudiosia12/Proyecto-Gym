package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.RutinaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaPredefinidaRepository extends JpaRepository<RutinaPredefinida, Long> {

    // Buscar rutina por objetivo y nivel
    Optional<RutinaPredefinida> findByObjetivoAndNivelAndActivoTrue(String objetivo, String nivel);

    // Buscar todas las rutinas activas
    List<RutinaPredefinida> findByActivoTrue();

    // Buscar por objetivo
    List<RutinaPredefinida> findByObjetivoAndActivoTrue(String objetivo);

    // Buscar por nivel
    List<RutinaPredefinida> findByNivelAndActivoTrue(String nivel);
}
