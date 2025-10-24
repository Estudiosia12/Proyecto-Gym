package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Reserva;
import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.ClaseGrupal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de reservas de clases grupales.
 * Proporciona métodos para consultar, validar y contabilizar reservas por miembro y clase.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * Busca las reservas de un miembro filtradas por estado.
     *
     * @param miembro El miembro del cual buscar reservas
     * @param estado Estado de la reserva ("ACTIVA", "CANCELADA")
     * @return Lista de reservas del miembro con el estado especificado
     */
    List<Reserva> findByMiembroAndEstado(Miembro miembro, String estado);

    /**
     * Busca si existe una reserva específica de un miembro para una clase con un estado determinado.
     * Útil para verificar si un miembro ya tiene una reserva activa en una clase antes de crear una nueva.
     *
     * @param miembro El miembro a verificar
     * @param claseGrupal La clase grupal a verificar
     * @param estado Estado de la reserva a buscar
     * @return Optional conteniendo la reserva si existe, empty en caso contrario
     */
    Optional<Reserva> findByMiembroAndClaseGrupalAndEstado(Miembro miembro, ClaseGrupal claseGrupal, String estado);

    /**
     * Cuenta el número de reservas activas de un miembro específico.
     * Permite controlar límites de reservas simultáneas por miembro.
     *
     * @param miembro El miembro del cual contar reservas activas
     * @return Cantidad de reservas activas del miembro
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.miembro = :miembro AND r.estado = 'ACTIVA'")
    Long countReservasActivasByMiembro(@Param("miembro") Miembro miembro);

    /**
     * Cuenta el número de reservas activas para una clase grupal específica.
     * Permite verificar disponibilidad de cupos en la clase.
     *
     * @param clase La clase grupal de la cual contar reservas
     * @return Cantidad de reservas activas en la clase
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.claseGrupal = :clase AND r.estado = 'ACTIVA'")
    Long countReservasActivasByClase(@Param("clase") ClaseGrupal clase);

    /**
     * Cuenta el total de reservas según su estado.
     * Útil para generar estadísticas generales del sistema de reservas.
     *
     * @param estado Estado a contabilizar (ej: "ACTIVA", "CANCELADA", "COMPLETADA")
     * @return Cantidad total de reservas con el estado especificado
     */
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
    Long countByEstado(@Param("estado") String estado);
}