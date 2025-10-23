package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Instructor;
import com.example.ProyectoGym.Repository.InstructorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InstructorService {

    @Autowired
    private InstructorRepository instructorRepository;

    // Obtener todos los instructores activos
    public List<Instructor> obtenerInstructoresActivos() {
        return instructorRepository.findByActivoTrue();
    }

    // Obtener todos los instructores
    public List<Instructor> obtenerTodosLosInstructores() {
        return instructorRepository.findAll();
    }

    // Obtener instructor por ID
    public Instructor obtenerInstructorPorId(Long id) {
        return instructorRepository.findById(id).orElse(null);
    }

    // Obtener instructor por DNI
    public Instructor obtenerInstructorPorDni(String dni) {
        return instructorRepository.findByDni(dni).orElse(null);
    }

    // Obtener instructores por especialidad
    public List<Instructor> obtenerInstructoresPorEspecialidad(String especialidad) {
        return instructorRepository.findByEspecialidadAndActivoTrue(especialidad);
    }

    // Crear instructor
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

    // Actualizar instructor
    public String actualizarInstructor(Long id, String nombre, String dni, String email,
                                       String telefono, String especialidad) {

        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        // Verificar si el DNI ya existe en otro instructor
        Instructor instructorConDni = instructorRepository.findByDni(dni).orElse(null);
        if (instructorConDni != null && !instructorConDni.getId().equals(id)) {
            return "ERROR: Ya existe otro instructor con ese DNI";
        }

        // Verificar si el email ya existe en otro instructor
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

    // Activar/Desactivar instructor
    public String cambiarEstadoInstructor(Long id, Boolean activo) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        instructor.setActivo(activo);
        instructorRepository.save(instructor);

        return "SUCCESS: Estado del instructor actualizado";
    }

    // Eliminar instructor
    public String eliminarInstructor(Long id) {
        Instructor instructor = instructorRepository.findById(id).orElse(null);

        if (instructor == null) {
            return "ERROR: Instructor no encontrado";
        }

        // Solo desactivamos en lugar de eliminar
        instructor.setActivo(false);
        instructorRepository.save(instructor);

        return "SUCCESS: Instructor desactivado";
    }

    // Contar instructores activos
    public long contarInstructoresActivos() {
        return instructorRepository.findByActivoTrue().size();
    }

    // Guardar instructor
    public String guardarInstructor(Instructor instructor) {
        try {
            instructorRepository.save(instructor);
            return "SUCCESS: Instructor guardado exitosamente";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
