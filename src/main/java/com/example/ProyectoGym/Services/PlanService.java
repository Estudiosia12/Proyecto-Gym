package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio para la gestion de planes de membresia del gimnasio.
 * Proporciona funcionalidades de creacion, actualizacion, consulta y administracion
 * de los diferentes planes disponibles para los miembros.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    /**
     * Obtiene todos los planes de membresia con estado activo.
     * Solo incluye planes disponibles para nuevas suscripciones.
     *
     * @return Lista de planes activos
     */
    public List<Plan> obtenerPlanesActivos() {
        return planRepository.findByActivoTrue();
    }

    /**
     * Obtiene todos los planes de membresia registrados en el sistema.
     *
     * @return Lista completa de planes (activos e inactivos)
     */
    public List<Plan> obtenerTodosLosPlanes() {
        return planRepository.findAll();
    }

    /**
     * Obtiene un plan especifico por su ID.
     *
     * @param id ID del plan a buscar
     * @return El plan encontrado o null si no existe
     */
    public Plan obtenerPlanPorId(Long id) {
        return planRepository.findById(id).orElse(null);
    }

    /**
     * Busca un plan por su nombre exacto.
     *
     * @param nombre Nombre del plan a buscar
     * @return El plan encontrado o null si no existe
     */
    public Plan obtenerPlanPorNombre(String nombre) {
        return planRepository.findByNombre(nombre).orElse(null);
    }

    /**
     * Guarda o actualiza un plan en la base de datos.
     * Valida que no exista otro plan con el mismo nombre al crear uno nuevo.
     *
     * @param plan Objeto plan a guardar
     * @return Mensaje de exito o error segun corresponda
     */
    public String guardarPlan(Plan plan) {
        try {
            if (plan.getId() == null && planRepository.existsByNombre(plan.getNombre())) {
                return "ERROR: Ya existe un plan con ese nombre";
            }

            planRepository.save(plan);
            return "SUCCESS: Plan guardado exitosamente";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Crea un nuevo plan de membresia en el sistema.
     * Valida que el nombre del plan no exista previamente.
     *
     * @param nombre Nombre del plan ("Basico", "Premium")
     * @param precio Precio mensual del plan
     * @param descripcion Descripcion detallada del plan
     * @param accesoClases Indica si incluye acceso a clases grupales
     * @param asesoriaPersonalizada Indica si incluye asesoria personalizada
     * @return Mensaje de exito o error segun corresponda
     */
    public String crearPlan(String nombre, BigDecimal precio, String descripcion,
                            Boolean accesoClases, Boolean asesoriaPersonalizada) {

        if (planRepository.existsByNombre(nombre)) {
            return "ERROR: Ya existe un plan con ese nombre";
        }

        Plan plan = new Plan(nombre, precio, descripcion, accesoClases, asesoriaPersonalizada);
        planRepository.save(plan);

        return "SUCCESS: Plan creado exitosamente";
    }

    /**
     * Actualiza los datos de un plan existente.
     * Valida que no haya conflicto de nombres con otros planes.
     *
     * @param id ID del plan a actualizar
     * @param nombre Nuevo nombre del plan
     * @param precio Nuevo precio
     * @param descripcion Nueva descripcion
     * @param accesoClases Nuevo valor de acceso a clases
     * @param asesoriaPersonalizada Nuevo valor de asesoria personalizada
     * @return Mensaje de exito o error segun corresponda
     */
    public String actualizarPlan(Long id, String nombre, BigDecimal precio, String descripcion,
                                 Boolean accesoClases, Boolean asesoriaPersonalizada) {

        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

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

    /**
     * Cambia el estado de activacion de un plan.
     *
     * @param id ID del plan
     * @param activo Nuevo estado (true para activo, false para inactivo)
     * @return Mensaje de exito o error segun corresponda
     */
    public String cambiarEstadoPlan(Long id, Boolean activo) {
        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        plan.setActivo(activo);
        planRepository.save(plan);

        return "SUCCESS: Estado del plan actualizado";
    }

    /**
     * Elimina logicamente un plan desactivandolo.
     * No realiza eliminacion fisica para mantener integridad referencial con miembros existentes.
     *
     * @param id ID del plan a eliminar
     * @return Mensaje de exito o error segun corresponda
     */
    public String eliminarPlan(Long id) {
        Plan plan = planRepository.findById(id).orElse(null);

        if (plan == null) {
            return "ERROR: Plan no encontrado";
        }

        plan.setActivo(false);
        planRepository.save(plan);

        return "SUCCESS: Plan desactivado";
    }

    /**
     * Cuenta el numero de planes activos disponibles.
     *
     * @return Cantidad de planes activos
     */
    public long contarPlanesActivos() {
        return planRepository.findByActivoTrue().size();
    }

    /**
     * Obtiene todos los planes que incluyen acceso a clases grupales.
     *
     * @return Lista de planes con acceso a clases incluido
     */
    public List<Plan> obtenerPlanesConClases() {
        return planRepository.findByAccesoClasesTrue();
    }
}