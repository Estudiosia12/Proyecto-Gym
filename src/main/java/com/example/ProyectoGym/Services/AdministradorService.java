package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    // Autenticar administrador
    public Administrador autenticarAdministrador(String usuario, String password) {
        return administradorRepository.findByUsuario(usuario)
                .filter(admin -> admin.getPassword().equals(password) && admin.getActivo())
                .orElse(null);
    }

    // Obtener administrador por usuario
    public Administrador obtenerPorUsuario(String usuario) {
        return administradorRepository.findByUsuario(usuario).orElse(null);
    }

    // Crear administrador
    public String crearAdministrador(String usuario, String password, String nombre, String email) {
        if (administradorRepository.existsByUsuario(usuario)) {
            return "ERROR: El usuario ya existe";
        }

        if (administradorRepository.existsByEmail(email)) {
            return "ERROR: El email ya existe";
        }

        Administrador admin = new Administrador(usuario, password, nombre, email);
        administradorRepository.save(admin);

        return "SUCCESS: Administrador creado exitosamente";
    }

    // Verificar si existe al menos un administrador
    public boolean existeAdministrador() {
        return administradorRepository.count() > 0;
    }
}
