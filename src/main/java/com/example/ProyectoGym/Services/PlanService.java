package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    // Obtener todos los planes activos
    public List<Plan> obtenerPlanesActivos() {
        return planRepository.findByActivoTrue();
    }

    // Obtener todos los planes (incluyendo inactivos)
    public List<Plan> obtenerTodosLosPlanes() {
        return planRepository.findAll();
    }

    // Obtener plan por ID
    public Plan obtenerPlanPorId(Long id) {
        return planRepository.findById(id).orElse(null);
    }

    // Obtener plan por nombre
    public Plan obtenerPlanPorNombre(String nombre) {
        return planRepository.findByNombre(nombre).orElse(null);
    }

    // Crear o actualizar plan
    public String guardarPlan(Plan plan) {
        try {
            // Validar si ya existe un plan con ese nombre
            if (plan.getId() == null && planRepository.existsByNombre(plan.getNombre())) {
                return "ERROR: Ya existe un plan con ese nombre";
            }

            planRepository.save(plan);
            return "SUCCESS: Plan guardado exitosamente";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Crear plan nuevo
    public String crearPlan(String nombre, BigDecimal precio, String descripcion,
                            Boolean accesoClases, Boolean asesoriaPersonalizada) {

        if (planRepository.existsByNombre(nombre)) {
            return "ERROR: Ya existe un plan con ese nombre";
        }

        Plan plan = new Plan(nombre, precio, descripcion, accesoClases, asesoriaPersonalizada);
        planRepository.save(plan);

        return "SUCCESS: Plan creado exitosamente";
    }

    // Actualizar plan existente
    public String actualizarPlan(Long id, String nombre, BigDecimal precio, String descripcion,
                                 Boolean accesoClases, Boolean asesoriaPersonalizada) {

        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        // Verificar si el nuevo nombre ya existe
        Plan planExistente = planRepository.findByNombre(nombre).orElse(null);
        if (planExistente != null && !planExistente.getId().equals(id)) {
            return "ERROR: Ya existe otro plan con ese nombre";
        }

        plan.setNombre(nombre);
        plan.setPrecio(precio);
        plan.setDescripcion(descripcion);
        plan.setAccesoClases(accesoClases);
        plan.setAsesoriaPersonalizada(asesoriaPersonalizada);

        planRepository.save(plan);
        return "SUCCESS: Plan actualizado exitosamente";
    }

    // Activar/Desactivar plan
    public String cambiarEstadoPlan(Long id, Boolean activo) {
        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        plan.setActivo(activo);
        planRepository.save(plan);

        return "SUCCESS: Estado del plan actualizado";
    }

    // Eliminar plan
    public String eliminarPlan(Long id) {
        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        // solo lo desactivamos
        plan.setActivo(false);
        planRepository.save(plan);

        return "SUCCESS: Plan desactivado";
    }

    // Contar planes activos
    public long contarPlanesActivos() {
        return planRepository.findByActivoTrue().size();
    }

    // Obtener planes con acceso a clases
    public List<Plan> obtenerPlanesConClases() {
        return planRepository.findByAccesoClasesTrue();
    }
}
