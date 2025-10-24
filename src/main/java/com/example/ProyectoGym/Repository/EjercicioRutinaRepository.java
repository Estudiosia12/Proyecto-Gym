package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.EjercicioRutina;
import com.example.ProyectoGym.Model.RutinaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestión de ejercicios dentro de rutinas predefinidas.
 * Permite consultar y organizar los ejercicios que componen cada rutina.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface EjercicioRutinaRepository extends JpaRepository<EjercicioRutina, Long> {

    /**
     * Obtiene todos los ejercicios de una rutina predefinida ordenados por su secuencia.
     * Los ejercicios se retornan en el orden en que deben ejecutarse.
     *
     * @param rutinaPredefinida La rutina de la cual obtener los ejercicios
     * @return Lista de ejercicios ordenados ascendentemente por el campo orden
     */
    List<EjercicioRutina> findByRutinaPredefinidaOrderByOrdenAsc(RutinaPredefinida rutinaPredefinida);

    /**
     * Cuenta el número total de ejercicios que contiene una rutina predefinida.
     *
     * @param rutinaPredefinida La rutina de la cual contar los ejercicios
     * @return Cantidad de ejercicios en la rutina
     */
    Long countByRutinaPredefinida(RutinaPredefinida rutinaPredefinida);
}