package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Repository.MiembroRepository;
import com.example.ProyectoGym.Repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MiembroService {

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private PlanRepository planRepository;

    // Registrar nuevo miembro
    public String registrarMiembro(String nombre, String email, String password, String dni,
                                   String telefono, LocalDate fechaNacimiento, String nombrePlan) {

        // Validar si el email ya existe
        if (miembroRepository.findByEmail(email).isPresent()) {
            return "ERROR: El email ya está registrado";
        }

        // Validar si el DNI ya existe
        if (miembroRepository.findByDni(dni).isPresent()) {
            return "ERROR: El DNI ya está registrado";
        }

        // Normalizar el nombre del plan
        String nombrePlanNormalizado = nombrePlan
                .replace("PLAN ", "")           // Quitar "PLAN "
                .replace("BÁSICO", "Basico")    // Convertir BÁSICO a Basico
                .replace("BASICO", "Basico")    // Convertir BASICO a Basico
                .replace("PREMIUM", "Premium")  // Convertir PREMIUM a Premium
                .trim();

        // Buscar el plan correspondiente
        Plan plan = planRepository.findByNombre(nombrePlanNormalizado).orElse(null);

        // Si no lo encuentra, intentar con capitalización estándar
        if (plan == null && nombrePlanNormalizado.length() > 0) {
            // Convertir a formato: Primera letra mayúscula, resto minúsculas
            String nombreCapitalizado = nombrePlanNormalizado.substring(0, 1).toUpperCase() +
                    nombrePlanNormalizado.substring(1).toLowerCase();
            plan = planRepository.findByNombre(nombreCapitalizado).orElse(null);
        }

        if (plan == null) {
            return "ERROR: Plan no encontrado. Recibido: '" + nombrePlan + "', Buscado: '" + nombrePlanNormalizado + "'";
        }

        // Crear miembro con todos los datos
        Miembro miembro = new Miembro(nombre, email, password, dni, telefono, fechaNacimiento, nombrePlanNormalizado);

        // Asignar el plan  antes de guardar
        miembro.setPlanDetalle(plan);
        miembro.setPlan(nombrePlanNormalizado); // Guardar nombre normalizado

        // Establecer fecha de vencimiento
        miembro.setFechaVencimiento(LocalDate.now().plusMonths(1));

        // GUARDAR
        miembroRepository.save(miembro);

        return "SUCCESS: Registro exitoso. Plan: " + plan.getNombre() + " - Precio: S/ " + plan.getPrecio();
    }

    // Autenticar miembro
    public Miembro autenticarMiembro(String dni, String password) {
        return miembroRepository.findByDni(dni)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }

    // Obtener miembro por email
    public Miembro obtenerMiembroPorEmail(String email) {
        return miembroRepository.findByEmail(email).orElse(null);
    }

    // Obtener miembro por DNI
    public Miembro obtenerMiembroPorDni(String dni) {
        return miembroRepository.findByDni(dni).orElse(null);
    }

    // Obtener miembro por ID
    public Miembro obtenerMiembroPorId(Long id) {
        return miembroRepository.findById(id).orElse(null);
    }

    // Obtener todos los miembros
    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroRepository.findAll();
    }

    // Obtener miembros activos
    public List<Miembro> obtenerMiembrosActivos() {
        return miembroRepository.findMiembrosActivos();
    }

    // Obtener miembros por plan
    public List<Miembro> obtenerMiembrosPorPlan(String plan) {
        return miembroRepository.findByPlan(plan);
    }

    // Contar miembros activos
    public long contarMiembrosActivos() {
        return miembroRepository.findMiembrosActivos().size();
    }

    // Contar total de miembros
    public long contarTotalMiembros() {
        return miembroRepository.count();
    }

    // Renovar membresía de un miembro
    public String renovarMembresia(Long miembroId, int meses) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        miembro.renovarMembresia(meses);
        miembroRepository.save(miembro);

        return "SUCCESS: Membresía renovada por " + meses + " mes(es)";
    }

    // Verificar y actualizar estados de membresías vencidas
    public void verificarMembresiasVencidas() {
        List<Miembro> miembros = miembroRepository.findAll();

        for (Miembro miembro : miembros) {
            if (miembro.estaVencida() && miembro.getActivo()) {
                miembro.setActivo(false);
                miembroRepository.save(miembro);
            }
        }
    }

    // Cambiar estado de miembro
    public String cambiarEstadoMiembro(Long miembroId, Boolean activo) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        miembro.setActivo(activo);
        miembroRepository.save(miembro);

        return "SUCCESS: Estado actualizado";
    }

    // Actualizar plan de miembro
    public String actualizarPlanMiembro(Long miembroId, String nombrePlan) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        Plan plan = planRepository.findByNombre(nombrePlan).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        miembro.setPlan(nombrePlan);
        miembro.setPlanDetalle(plan);
        miembroRepository.save(miembro);

        return "SUCCESS: Plan actualizado";
    }

    // Actualizar datos del miembro
    public String actualizarMiembro(Long id, String nombre, String email, String telefono) {
        Miembro miembro = miembroRepository.findById(id).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        // Verificar si el email ya existe en otro miembro
        Miembro miembroConEmail = miembroRepository.findByEmail(email).orElse(null);
        if (miembroConEmail != null && !miembroConEmail.getId().equals(id)) {
            return "ERROR: El email ya está en uso";
        }

        miembro.setNombre(nombre);
        miembro.setEmail(email);
        miembro.setTelefono(telefono);

        miembroRepository.save(miembro);
        return "SUCCESS: Datos actualizados";
    }

    // Obtener miembros con membresía por vencer
    public List<Miembro> obtenerMiembrosProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate dentroDe7Dias = hoy.plusDays(7);
        return miembroRepository.findByFechaVencimientoBetween(hoy, dentroDe7Dias);
    }

    // Contar miembros por plan
    public long contarMiembrosPorPlan(String plan) {
        return miembroRepository.findByPlan(plan).size();
    }
}