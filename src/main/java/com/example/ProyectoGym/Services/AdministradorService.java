package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio para la gestion de administradores del sistema.
 * Proporciona funcionalidades de autenticacion, registro y consulta de administradores.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    /**
     * Autentica un administrador verificando sus credenciales y estado activo.
     * Valida que el usuario exista, la contrasena sea correcta y la cuenta este activa.
     *
     * @param usuario Nombre de usuario del administrador
     * @param password Contrasena del administrador
     * @return El administrador autenticado si las credenciales son validas y esta activo, null en caso contrario
     */
    public Administrador autenticarAdministrador(String usuario, String password) {
        return administradorRepository.findByUsuario(usuario)
                .filter(admin -> admin.getPassword().equals(password) && admin.getActivo())
                .orElse(null);
    }

    /**
     * Obtiene un administrador por su nombre de usuario.
     *
     * @param usuario Nombre de usuario a buscar
     * @return El administrador encontrado o null si no existe
     */
    public Administrador obtenerPorUsuario(String usuario) {
        return administradorRepository.findByUsuario(usuario).orElse(null);
    }

    /**
     * Crea un nuevo administrador en el sistema.
     * Valida que el usuario y email no existan previamente en la base de datos.
     *
     * @param usuario Nombre de usuario unico para el administrador
     * @param password Contrasena del administrador
     * @param nombre Nombre completo del administrador
     * @param email Correo electronico unico del administrador
     * @return Mensaje de exito si se creo correctamente, mensaje de error si el usuario o email ya existen
     */
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

    /**
     * Verifica si existe al menos un administrador registrado en el sistema.
     * Util para determinar si es necesario crear un administrador inicial.
     *
     * @return true si existe al menos un administrador, false en caso contrario
     */
    public boolean existeAdministrador() {
        return administradorRepository.count() > 0;
    }
}