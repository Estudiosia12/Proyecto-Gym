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

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    // Buscar reservas activas de un miembro
    List<Reserva> findByMiembroAndEstado(Miembro miembro, String estado);

    // Buscar si existe una reserva activa para un miembro en una clase específica
    Optional<Reserva> findByMiembroAndClaseGrupalAndEstado(Miembro miembro, ClaseGrupal claseGrupal, String estado);

    // Contar reservas activas de un miembro
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.miembro = :miembro AND r.estado = 'ACTIVA'")
    Long countReservasActivasByMiembro(@Param("miembro") Miembro miembro);

    // Contar reservas activas para una clase
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.claseGrupal = :clase AND r.estado = 'ACTIVA'")
    Long countReservasActivasByClase(@Param("clase") ClaseGrupal clase);

    // NUEVO: Contar todas las reservas por estado
    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.estado = :estado")
    Long countByEstado(@Param("estado") String estado);
}