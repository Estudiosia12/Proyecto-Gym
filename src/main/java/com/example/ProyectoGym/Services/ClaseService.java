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

/**
 * Servicio para la gestion de clases grupales y reservas.
 * Proporciona funcionalidades de creacion, actualizacion, reserva y consulta de clases,
 * asi como la gestion de cupos y asignacion de instructores.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class ClaseService {

    @Autowired
    private ClaseGrupalRepository claseGrupalRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    /**
     * Obtiene todas las clases grupales con estado activo.
     *
     * @return Lista de clases activas disponibles para reserva
     */
    public List<ClaseGrupal> obtenerClasesActivas() {
        return claseGrupalRepository.findByActivaTrue();
    }

    /**
     * Obtiene todas las clases grupales registradas en el sistema.
     *
     * @return Lista completa de clases (activas e inactivas)
     */
    public List<ClaseGrupal> obtenerTodasLasClases() {
        return claseGrupalRepository.findAll();
    }

    /**
     * Obtiene una clase grupal especifica por su ID.
     *
     * @param id ID de la clase a buscar
     * @return La clase encontrada o null si no existe
     */
    public ClaseGrupal obtenerClasePorId(Long id) {
        Optional<ClaseGrupal> clase = claseGrupalRepository.findById(id);
        return clase.orElse(null);
    }

    /**
     * Permite a un miembro reservar una clase grupal.
     * Valida que el miembro tenga plan Premium, que la clase exista,
     * que no tenga una reserva previa activa y que haya cupos disponibles.
     *
     * @param miembro Miembro que desea reservar
     * @param claseId ID de la clase a reservar
     * @return Mensaje de exito o error segun corresponda
     */
    public String reservarClase(Miembro miembro, Long claseId) {
        if (miembro.getPlan() == null || !miembro.getPlan().equalsIgnoreCase("Premium")) {
            return "ERROR: Solo los miembros Premium pueden reservar clases";
        }

        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(claseId);
        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        ClaseGrupal clase = claseOpt.get();

        Optional<Reserva> reservaExistente = reservaRepository.findByMiembroAndClaseGrupalAndEstado(
                miembro, clase, "ACTIVA");

        if (reservaExistente.isPresent()) {
            return "ERROR: Ya tienes una reserva activa para esta clase";
        }

        if (clase.getCapacidad() != null) {
            Long reservasActivas = reservaRepository.countReservasActivasByClase(clase);
            if (reservasActivas >= clase.getCapacidad()) {
                return "ERROR: La clase ha alcanzado su capacidad máxima";
            }
        }

        try {
            Reserva nuevaReserva = new Reserva(miembro, clase);
            reservaRepository.save(nuevaReserva);
            return "SUCCESS: Reserva realizada exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo completar la reserva";
        }
    }

    /**
     * Cancela una reserva de clase grupal.
     * Valida que la reserva exista, pertenezca al miembro y este activa.
     *
     * @param reservaId ID de la reserva a cancelar
     * @param miembro Miembro que solicita la cancelacion
     * @return Mensaje de exito o error segun corresponda
     */
    public String cancelarReserva(Long reservaId, Miembro miembro) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);

        if (!reservaOpt.isPresent()) {
            return "ERROR: Reserva no encontrada";
        }

        Reserva reserva = reservaOpt.get();

        if (!reserva.getMiembro().getId().equals(miembro.getId())) {
            return "ERROR: No puedes cancelar esta reserva";
        }

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

    /**
     * Obtiene todas las reservas activas de un miembro.
     *
     * @param miembro Miembro del cual obtener reservas
     * @return Lista de reservas activas del miembro
     */
    public List<Reserva> obtenerReservasActivas(Miembro miembro) {
        return reservaRepository.findByMiembroAndEstado(miembro, "ACTIVA");
    }

    /**
     * Cuenta el numero de reservas activas que tiene un miembro.
     *
     * @param miembro Miembro del cual contar reservas
     * @return Cantidad de reservas activas
     */
    public Long contarReservasActivas(Miembro miembro) {
        return reservaRepository.countReservasActivasByMiembro(miembro);
    }

    /**
     * Verifica si un miembro tiene permiso para reservar clases grupales.
     * Solo miembros con plan Premium pueden reservar.
     *
     * @param miembro Miembro a verificar
     * @return true si puede reservar, false en caso contrario
     */
    public boolean puedeReservar(Miembro miembro) {
        return miembro.getPlan() != null && miembro.getPlan().equalsIgnoreCase("Premium");
    }

    /**
     * Calcula los cupos disponibles para una clase grupal.
     *
     * @param clase Clase de la cual calcular cupos
     * @return Numero de cupos disponibles, o null si no hay limite de capacidad
     */
    public Integer calcularCuposDisponibles(ClaseGrupal clase) {
        if (clase.getCapacidad() == null) {
            return null;
        }
        Long reservasActivas = reservaRepository.countReservasActivasByClase(clase);
        return clase.getCapacidad() - reservasActivas.intValue();
    }

    /**
     * Crea una nueva clase grupal en el sistema.
     * Valida que no exista otra clase con el mismo nombre.
     *
     * @param nombre Nombre de la clase
     * @param descripcion Descripcion de la clase
     * @param diaSemana Dia de la semana en que se realiza
     * @param horaInicio Hora de inicio
     * @param duracion Duracion en minutos
     * @param capacidad Capacidad maxima de participantes
     * @param imagenUrl URL de la imagen representativa
     * @param instructorId ID del instructor asignado (opcional)
     * @return Mensaje de exito o error segun corresponda
     */
    public String crearClase(String nombre, String descripcion, String diaSemana,
                             String horaInicio, Integer duracion, Integer capacidad,
                             String imagenUrl, Long instructorId) {

        Optional<ClaseGrupal> claseExistente = claseGrupalRepository.findByNombre(nombre);
        if (claseExistente.isPresent()) {
            return "ERROR: Ya existe una clase con ese nombre";
        }

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

    /**
     * Actualiza los datos de una clase grupal existente.
     * Valida que la clase exista y que no haya conflicto de nombres.
     *
     * @param id ID de la clase a actualizar
     * @param nombre Nuevo nombre de la clase
     * @param descripcion Nueva descripcion
     * @param diaSemana Nuevo dia de la semana
     * @param horaInicio Nueva hora de inicio
     * @param duracion Nueva duracion
     * @param capacidad Nueva capacidad
     * @param imagenUrl Nueva URL de imagen
     * @param instructorId Nuevo ID de instructor
     * @return Mensaje de exito o error segun corresponda
     */
    public String actualizarClase(Long id, String nombre, String descripcion, String diaSemana,
                                  String horaInicio, Integer duracion, Integer capacidad,
                                  String imagenUrl, Long instructorId) {

        Optional<ClaseGrupal> claseOpt = claseGrupalRepository.findById(id);
        if (!claseOpt.isPresent()) {
            return "ERROR: Clase no encontrada";
        }

        ClaseGrupal clase = claseOpt.get();

        Optional<ClaseGrupal> claseConNombre = claseGrupalRepository.findByNombre(nombre);
        if (claseConNombre.isPresent() && !claseConNombre.get().getId().equals(id)) {
            return "ERROR: Ya existe otra clase con ese nombre";
        }

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

    /**
     * Cambia el estado de activacion de una clase grupal.
     *
     * @param id ID de la clase
     * @param activa Nuevo estado (true para activa, false para inactiva)
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Asigna un instructor a una clase grupal.
     *
     * @param claseId ID de la clase
     * @param instructorId ID del instructor a asignar
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Cuenta el numero total de clases activas en el gimnasio.
     *
     * @return Cantidad de clases activas
     */
    public long contarClasesActivas() {
        return claseGrupalRepository.countClasesActivas();
    }

    /**
     * Cuenta el numero total de reservas activas en el sistema.
     *
     * @return Cantidad de reservas con estado ACTIVA
     */
    public long contarTodasLasReservasActivas() {
        return reservaRepository.countByEstado("ACTIVA");
    }

    /**
     * Obtiene todas las clases asignadas a un instructor especifico.
     *
     * @param instructorId ID del instructor
     * @return Lista de clases del instructor, vacia si no existe el instructor
     */
    public List<ClaseGrupal> obtenerClasesPorInstructor(Long instructorId) {
        Optional<Instructor> instructorOpt = instructorRepository.findById(instructorId);
        if (!instructorOpt.isPresent()) {
            return List.of();
        }
        return claseGrupalRepository.findByInstructor(instructorOpt.get());
    }
}