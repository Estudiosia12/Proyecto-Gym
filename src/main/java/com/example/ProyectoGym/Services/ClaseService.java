package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.ClaseGrupal;
import com.example.ProyectoGym.Model.Instructor;
import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.Reserva;
import com.example.ProyectoGym.Repository.ClaseGrupalRepository;
import com.example.ProyectoGym.Repository.InstructorRepository;
import com.example.ProyectoGym.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClaseService {

    @Autowired
    private ClaseGrupalRepository claseGrupalRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    // Obtener todas las clases activas
    public List<ClaseGrupal> obtenerClasesActivas() {
        return claseGrupalRepository.findByActivaTrue();
    }

    // Obtener todas las clases
    public List<ClaseGrupal> obtenerTodasLasClases() {
        return claseGrupalRepository.findAll();
    }

    // Obtener una clase por ID
    public ClaseGrupal obtenerClasePorId(Long id) {
        Optional<ClaseGrupal> clase = claseGrupalRepository.findById(id);
        return clase.orElse(null);
    }

    // Reservar una clase
    public String reservarClase(Miembro miembro, Long claseId) {
        // Validar que el miembro sea Premium
        if (miembro.getPlan() == null || !miembro.getPlan().equalsIgnoreCase("Premium")) {
            return "ERROR: Solo los miembros Premium pueden reservar clases";
        }

        // Buscar la clase
        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(claseId);
        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        ClaseGrupal clase = claseOpt.get();

        // Verificar si ya tiene una reserva activa para esta clase
        Optional<Reserva> reservaExistente = reservaRepository.findByMiembroAndClaseGrupalAndEstado(
                miembro, clase, "ACTIVA");

        if (reservaExistente.isPresent()) {
            return "ERROR: Ya tienes una reserva activa para esta clase";
        }

        // Verificar capacidad
        if (clase.getCapacidad() != null) {
            Long reservasActivas = reservaRepository.countReservasActivasByClase(clase);
            if (reservasActivas >= clase.getCapacidad()) {
                return "ERROR: La clase ha alcanzado su capacidad máxima";
            }
        }

        // Crear la reserva
        try {
            Reserva nuevaReserva = new Reserva(miembro, clase);
            reservaRepository.save(nuevaReserva);
            return "SUCCESS: Reserva realizada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo completar la reserva";
        }
    }

    // Cancelar una reserva
    public String cancelarReserva(Long reservaId, Miembro miembro) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);

        if (!reservaOpt.isPresent()) {
            return "ERROR: Reserva no encontrada";
        }

        Reserva reserva = reservaOpt.get();

        // Verificar que la reserva pertenece al miembro
        if (!reserva.getMiembro().getId().equals(miembro.getId())) {
            return "ERROR: No puedes cancelar esta reserva";
        }

        // Verificar que la reserva esté activa
        if (!reserva.getEstado().equals("ACTIVA")) {
            return "ERROR: Esta reserva ya fue cancelada";
        }

        try {
            reserva.setEstado("CANCELADA");
            reservaRepository.save(reserva);
            return "SUCCESS: Reserva cancelada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo cancelar la reserva";
        }
    }

    // Obtener reservas activas de un miembro
    public List<Reserva> obtenerReservasActivas(Miembro miembro) {
        return reservaRepository.findByMiembroAndEstado(miembro, "ACTIVA");
    }

    // Contar reservas activas de un miembro
    public Long contarReservasActivas(Miembro miembro) {
        return reservaRepository.countReservasActivasByMiembro(miembro);
    }

    // Verificar si un miembro puede reservar
    public boolean puedeReservar(Miembro miembro) {
        return miembro.getPlan() != null && miembro.getPlan().equalsIgnoreCase("Premium");
    }

    // Calcular cupos disponibles para una clase
    public Integer calcularCuposDisponibles(ClaseGrupal clase) {
        if (clase.getCapacidad() == null) {
            return null; // Sin límite
        }
        Long reservasActivas = reservaRepository.countReservasActivasByClase(clase);
        return clase.getCapacidad() - reservasActivas.intValue();
    }


    // Crear clase grupal
    public String crearClase(String nombre, String descripcion, String diaSemana,
                             String horaInicio, Integer duracion, Integer capacidad,
                             String imagenUrl, Long instructorId) {

        // Validar que no exista una clase con el mismo nombre
        Optional<ClaseGrupal> claseExistente = claseGrupalRepository.findByNombre(nombre);
        if (claseExistente.isPresent()) {
            return "ERROR: Ya existe una clase con ese nombre";
        }

        // Obtener instructor (opcional)
        Instructor instructor = null;
        if (instructorId != null) {
            Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);
            if (!instructorOpt.isPresent()) {
                return "ERROR: Instructor no encontrado";
            }
            instructor = instructorOpt.get();
        }

        try {
            ClaseGrupal clase = new ClaseGrupal(nombre, descripcion, diaSemana, horaInicio,
                    duracion, capacidad, imagenUrl, instructor);
            claseGrupalRepository.save(clase);
            return "SUCCESS: Clase creada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo crear la clase";
        }
    }

    // Actualizar clase grupal
    public String actualizarClase(Long id, String nombre, String descripcion, String diaSemana,
                                  String horaInicio, Integer duracion, Integer capacidad,
                                  String imagenUrl, Long instructorId) {

        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(id);
        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        ClaseGrupal clase = claseOpt.get();

        // Verificar si el nuevo nombre ya existe
        Optional<ClaseGrupal> claseConNombre = claseGrupalRepository.findByNombre(nombre);
        if (claseConNombre.isPresent() && !claseConNombre.get().getId().equals(id)) {
            return "ERROR: Ya existe otra clase con ese nombre";
        }

        // Obtener instructor
        Instructor instructor = null;
        if (instructorId != null) {
            Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);
            if (!instructorOpt.isPresent()) {
                return "ERROR: Instructor no encontrado";
            }
            instructor = instructorOpt.get();
        }

        try {
            clase.setNombre(nombre);
            clase.setDescripcion(descripcion);
            clase.setDiaSemana(diaSemana);
            clase.setHoraInicio(horaInicio);
            clase.setDuracion(duracion);
            clase.setCapacidad(capacidad);
            clase.setImagenUrl(imagenUrl);
            clase.setInstructor(instructor);

            claseGrupalRepository.save(clase);
            return "SUCCESS: Clase actualizada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo actualizar la clase";
        }
    }

    // Cambiar estado de clase
    public String cambiarEstadoClase(Long id, Boolean activa) {
        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(id);

        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        try {
            ClaseGrupal clase = claseOpt.get();
            clase.setActiva(activa);
            claseGrupalRepository.save(clase);
            return "SUCCESS: Estado de clase actualizado";
        } catch (Exception e) {
            return "ERROR: No se pudo actualizar el estado";
        }
    }

    // Asignar instructor a clase
    public String asignarInstructor(Long claseId, Long instructorId) {
        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(claseId);

        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);

        if (!instructorOpt.isPresent()) {
            return "ERROR: Instructor no encontrado";
        }

        try {
            ClaseGrupal clase = claseOpt.get();
            clase.setInstructor(instructorOpt.get());
            claseGrupalRepository.save(clase);
            return "SUCCESS: Instructor asignado a la clase";
        } catch (Exception e) {
            return "ERROR: No se pudo asignar el instructor";
        }
    }

    // Contar total de clases activas
    public long contarClasesActivas() {
        return claseGrupalRepository.countClasesActivas();
    }

    // Contar total de reservas activas
    public long contarTodasLasReservasActivas() {
        return reservaRepository.countByEstado("ACTIVA");
    }

    // Obtener clases por instructor
    public List<ClaseGrupal> obtenerClasesPorInstructor(Long instructorId) {
        Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);
        if (!instructorOpt.isPresent()) {
            return List.of();
        }
        return claseGrupalRepository.findByInstructor(instructorOpt.get());
    }
}