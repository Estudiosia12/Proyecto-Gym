
package com.example.ProyectoGym.Repository;

import com.example.ProyectoGym.Model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByUsuario(String usuario);

    Optional<Administrador> findByEmail(String email);

    boolean existsByUsuario(String usuario);

    boolean existsByEmail(String email);
}
