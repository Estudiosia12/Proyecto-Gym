package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.RutinaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de rutinas de entrenamiento predefinidas.
 * Proporciona métodos para consultar rutinas por objetivo, nivel de dificultad y estado.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface RutinaPredefinidaRepository extends JpaRepository<RutinaPredefinida, Long> {

    /**
     * Busca una rutina predefinida activa que coincida con un objetivo y nivel específicos.
     * Útil para asignar la rutina más adecuada según las características del miembro.
     *
     * @param objetivo Objetivo de la rutina (ej: "Bajar Peso", "Tonificar", "Aumentar Masa Muscular")
     * @param nivel Nivel de dificultad (ej: "Principiante", "Intermedio", "Avanzado")
     * @return Optional conteniendo la rutina si existe, empty en caso contrario
     */
    Optional<RutinaPredefinida> findByObjetivoAndNivelAndActivoTrue(String objetivo, String nivel);

    /**
     * Obtiene todas las rutinas predefinidas con estado activo.
     *
     * @return Lista de rutinas activas disponibles para asignación
     */
    List<RutinaPredefinida> findByActivoTrue();

    /**
     * Busca rutinas activas filtradas por objetivo de entrenamiento.
     *
     * @param objetivo Objetivo de la rutina a buscar
     * @return Lista de rutinas activas con el objetivo especificado
     */
    List<RutinaPredefinida> findByObjetivoAndActivoTrue(String objetivo);

    /**
     * Busca rutinas activas filtradas por nivel de dificultad.
     *
     * @param nivel Nivel de dificultad a buscar
     * @return Lista de rutinas activas con el nivel especificado
     */
    List<RutinaPredefinida> findByNivelAndActivoTrue(String nivel);
}