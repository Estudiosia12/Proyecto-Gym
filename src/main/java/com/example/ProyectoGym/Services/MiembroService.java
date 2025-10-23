package com.example.ProyectoGym.Services;
import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Repository.MiembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MiembroService {
    @Autowired
    private MiembroRepository miembroRepository;

    //Para registrar un nuevo miembro
    public String registrarMiembro(String nombre, String email, String password,
                                   String dni, String telefono, LocalDate fechaNacimiento, String plan) {

        if (miembroRepository.existsByDni(dni)) {
            return "ERROR: El DNI ya está registrado";
        }

        if (miembroRepository.existsByEmail(email)) {
            return "ERROR: El email ya está registrado";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            return "ERROR: El nombre es obligatorio";
        }

        if (dni == null || dni.length() != 8) {
            return "ERROR: El DNI debe tener 8 dígitos";
        }

        if (password == null || password.length() < 6) {
            return "ERROR: La contraseña debe tener al menos 6 caracteres";
        }

        Miembro nuevoMiembro = new Miembro(nombre, email, password, dni, telefono, fechaNacimiento, plan);

        try {
            miembroRepository.save(nuevoMiembro);
            return "SUCCESS: Miembro registrado exitosamente";
        } catch (Exception e) {
            return "ERROR: No se pudo registrar el miembro";
        }
    }

    // Para el Login
    public Miembro autenticarMiembro(String dni, String password) {

        if (dni == null || dni.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return null;
        }

        Optional<Miembro> miembroOpt = miembroRepository.findByDniAndPassword(dni, password);

        if (miembroOpt.isPresent()) {
            Miembro miembro = miembroOpt.get();
            if (miembro.getActivo()) {
                return miembro;
            }
        }

        return null;
    }

    // Busca un miembro por su DNI
    public Miembro buscarPorDni(String dni) {
        Optional<Miembro> miembroOpt = miembroRepository.findByDni(dni);
        return miembroOpt.orElse(null);
    }

    // Obtenemos informacion de la membresia
    public String obtenerEstadoMembresia(Long idMiembro) {
        Optional<Miembro> miembroOpt = miembroRepository.findById(idMiembro);
        if (miembroOpt.isPresent()) {
            Miembro miembro = miembroOpt.get();
            return miembro.getActivo() ? "ACTIVO" : "INACTIVO";
        }
        return "NO ENCONTRADO";
    }

    // Cuenta la cantidad de miembros por plan
    public Long contarMiembrosPorPlan(String plan) {
        return miembroRepository.countByPlan(plan);
    }

    // Permite validar si un dni ya existe (útil para AJAX)
    public boolean existeDni(String dni) {
        return miembroRepository.existsByDni(dni);
    }

    // Metodo para validar si un email existe
    public boolean existeEmail(String email) {
        return miembroRepository.existsByEmail(email);
    }
}
