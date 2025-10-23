package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.EjercicioRutina;
import com.example.ProyectoGym.Model.RutinaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjercicioRutinaRepository extends JpaRepository<EjercicioRutina, Long> {

    // Buscar ejercicios de una rutina ordenados
    List<EjercicioRutina> findByRutinaPredefinidaOrderByOrdenAsc(RutinaPredefinida rutinaPredefinida);

    // Contar ejercicios de una rutina
    Long countByRutinaPredefinida(RutinaPredefinida rutinaPredefinida);
}
