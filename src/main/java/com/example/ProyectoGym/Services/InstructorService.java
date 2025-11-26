package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Instructor;
import com.example.ProyectoGym.Repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestion de instructores del gimnasio.
 * Proporciona funcionalidades de registro, actualizacion, consulta y administracion
 * del estado de los instructores.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class InstructorService {

    @Autowired
    private InstructorRepository instructorRepository;

    /**
     * Obtiene todos los instructores con estado activo.
     *
     * @return Lista de instructores activos
     */
    public List<Instructor> obtenerInstructoresActivos() {
        return instructorRepository.findByActivoTrue();
    }

    /**
     * Obtiene todos los instructores registrados en el sistema.
     *
     * @return Lista completa de instructores (activos e inactivos)
     */
    public List<Instructor> obtenerTodosLosInstructores() {
        return instructorRepository.findAll();
    }

    /**
     * Obtiene un instructor especifico por su ID.
     *
     * @param id ID del instructor a buscar
     * @return El instructor encontrado o null si no existe
     */
    public Instructor obtenerInstructorPorId(Long id) {
        return instructorRepository.findById(id).orElse(null);
    }

    /**
     * Busca un instructor por su numero de DNI.
     *
     * @param dni DNI del instructor a buscar
     * @return El instructor encontrado o null si no existe
     */
    public Instructor obtenerInstructorPorDni(String dni) {
        return instructorRepository.findByDni(dni).orElse(null);
    }

    /**
     * Obtiene instructores activos filtrados por especialidad.
     *
     * @param especialidad Especialidad a buscar (ej: "Yoga", "CrossFit", "Spinning")
     * @return Lista de instructores activos con la especialidad especificada
     */
    public List<Instructor> obtenerInstructoresPorEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadAndActivoTrue(especialidad);
    }

    /**
     * Crea un nuevo instructor en el sistema.
     * Valida que el DNI y email no existan previamente en la base de datos.
     *
     * @param nombre Nombre completo del instructor
     * @param dni Documento Nacional de Identidad unico
     * @param email Correo electronico unico
     * @param telefono Numero de telefono de contacto
     * @param especialidad Area de especializacion del instructor
     * @return Mensaje de exito si se creo correctamente, mensaje de error si el DNI o email ya existen
     */
    public String crearInstructor(String nombre, String dni, String email,
                                  String telefono, String especialidad) {

        if (instructorRepository.existsByDni(dni)) {
            return "ERROR: Ya existe un instructor con ese DNI";
        }

        if (instructorRepository.existsByEmail(email)) {
            return "ERROR: Ya existe un instructor con ese email";
        }

        Instructor instructor = new Instructor(nombre, dni, email, telefono, especialidad);
        instructorRepository.save(instructor);

        return "SUCCESS: Instructor creado exitosamente";
    }

    /**
     * Actualiza los datos de un instructor existente.
     * Valida que no haya conflictos de DNI y email con otros instructores.
     *
     * @param id ID del instructor a actualizar
     * @param nombre Nuevo nombre del instructor
     * @param dni Nuevo DNI
     * @param email Nuevo email
     * @param telefono Nuevo telefono
     * @param especialidad Nueva especialidad
     * @return Mensaje de exito o error segun corresponda
     */
    public String actualizarInstructor(Long id, String nombre, String dni, String email,
                                       String telefono, String especialidad) {

        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        Instructor instructorConDni = instructorRepository.findByDni(dni).orElse(null);
        if (instructorConDni != null && !instructorConDni.getId().equals(id)) {
            return "ERROR: Ya existe otro instructor con ese DNI";
        }

        Instructor instructorConEmail = instructorRepository.findByEmail(email).orElse(null);
        if (instructorConEmail != null && !instructorConEmail.getId().equals(id)) {
            return "ERROR: Ya existe otro instructor con ese email";
        }

        instructor.setNombre(nombre);
        instructor.setDni(dni);
        instructor.setEmail(email);
        instructor.setTelefono(telefono);
        instructor.setEspecialidad(especialidad);

        instructorRepository.save(instructor);
        return "SUCCESS: Instructor actualizado exitosamente";
    }

    /**
     * Cambia el estado de activacion de un instructor.
     *
     * @param id ID del instructor
     * @param activo Nuevo estado (true para activo, false para inactivo)
     * @return Mensaje de exito o error segun corresponda
     */
    public String cambiarEstadoInstructor(Long id, Boolean activo) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        instructor.setActivo(activo);
        instructorRepository.save(instructor);

        return "SUCCESS: Estado del instructor actualizado";
    }

    /**
     * Elimina logicamente un instructor desactivandolo.
     * No realiza eliminacion fisica de la base de datos para mantener integridad referencial.
     *
     * @param id ID del instructor a eliminar
     * @return Mensaje de exito o error segun corresponda
     */
    public String eliminarInstructor(Long id) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        instructor.setActivo(false);
        instructorRepository.save(instructor);

        return "SUCCESS: Instructor desactivado";
    }

    /**
     * Cuenta el numero total de instructores activos en el gimnasio.
     *
     * @return Cantidad de instructores activos
     */
    public long contarInstructoresActivos() {
        return instructorRepository.findByActivoTrue().size();
    }

    /**
     * Guarda o actualiza un instructor en la base de datos.
     * Metodo generico para persistencia de instructores.
     *
     * @param instructor Objeto instructor a guardar
     * @return Mensaje de exito o error con detalles de la excepcion
     */
    public String guardarInstructor(Instructor instructor) {
        try {
            instructorRepository.save(instructor);
            return "SUCCESS: Instructor guardado exitosamente";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}