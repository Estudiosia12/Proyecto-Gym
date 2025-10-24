package com.example.ProyectoGym.Services;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Repository.MiembroRepository;
import com.example.ProyectoGym.Repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la gestion de miembros del gimnasio.
 * Proporciona funcionalidades de registro, autenticacion, actualizacion de datos,
 * gestion de membresias y consultas de miembros.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Service
public class MiembroService {

    @Autowired
    private MiembroRepository miembroRepository;

    @Autowired
    private PlanRepository planRepository;

    /**
     * Registra un nuevo miembro en el sistema.
     * Valida que el email y DNI no existan previamente, normaliza el nombre del plan,
     * busca el plan correspondiente y establece la fecha de vencimiento inicial.
     *
     * @param nombre Nombre completo del miembro
     * @param email Correo electronico unico del miembro
     * @param password Contrasena de acceso
     * @param dni Documento Nacional de Identidad unico
     * @param telefono Numero de telefono de contacto
     * @param fechaNacimiento Fecha de nacimiento del miembro
     * @param nombrePlan Nombre del plan de membresia seleccionado
     * @return Mensaje de exito con detalles del plan o mensaje de error segun corresponda
     */
    public String registrarMiembro(String nombre, String email, String password, String dni,
                                   String telefono, LocalDate fechaNacimiento, String nombrePlan) {

        if (miembroRepository.findByEmail(email).isPresent()) {
            return "ERROR: El email ya está registrado";
        }

        if (miembroRepository.findByDni(dni).isPresent()) {
            return "ERROR: El DNI ya está registrado";
        }

        String nombrePlanNormalizado = nombrePlan
                .replace("PLAN ", "")
                .replace("BÁSICO", "Basico")
                .replace("BASICO", "Basico")
                .replace("PREMIUM", "Premium")
                .trim();

        Plan plan = planRepository.findByNombre(nombrePlanNormalizado).orElse(null);

        if (plan == null && nombrePlanNormalizado.length() > 0) {
            String nombreCapitalizado = nombrePlanNormalizado.substring(0, 1).toUpperCase() +
                    nombrePlanNormalizado.substring(1).toLowerCase();
            plan = planRepository.findByNombre(nombreCapitalizado).orElse(null);
        }

        if (plan == null) {
            return "ERROR: Plan no encontrado. Recibido: '" + nombrePlan + "', Buscado: '" + nombrePlanNormalizado + "'";
        }

        Miembro miembro = new Miembro(nombre, email, password, dni, telefono, fechaNacimiento, nombrePlanNormalizado);

        miembro.setPlanDetalle(plan);
        miembro.setPlan(nombrePlanNormalizado);
        miembro.setFechaVencimiento(LocalDate.now().plusMonths(1));

        miembroRepository.save(miembro);

        return "SUCCESS: Registro exitoso. Plan: " + plan.getNombre() + " - Precio: S/ " + plan.getPrecio();
    }

    /**
     * Autentica un miembro verificando sus credenciales.
     * Valida que el DNI exista y la contrasena coincida.
     *
     * @param dni DNI del miembro
     * @param password Contrasena del miembro
     * @return El miembro autenticado si las credenciales son correctas, null en caso contrario
     */
    public Miembro autenticarMiembro(String dni, String password) {
        return miembroRepository.findByDni(dni)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }

    /**
     * Busca un miembro por su correo electronico.
     *
     * @param email Correo electronico a buscar
     * @return El miembro encontrado o null si no existe
     */
    public Miembro obtenerMiembroPorEmail(String email) {
        return miembroRepository.findByEmail(email).orElse(null);
    }

    /**
     * Busca un miembro por su numero de DNI.
     *
     * @param dni DNI a buscar
     * @return El miembro encontrado o null si no existe
     */
    public Miembro obtenerMiembroPorDni(String dni) {
        return miembroRepository.findByDni(dni).orElse(null);
    }

    /**
     * Obtiene un miembro especifico por su ID.
     *
     * @param id ID del miembro a buscar
     * @return El miembro encontrado o null si no existe
     */
    public Miembro obtenerMiembroPorId(Long id) {
        return miembroRepository.findById(id).orElse(null);
    }

    /**
     * Obtiene todos los miembros registrados en el sistema.
     *
     * @return Lista completa de miembros (activos e inactivos)
     */
    public List<Miembro> obtenerTodosLosMiembros() {
        return miembroRepository.findAll();
    }

    /**
     * Obtiene todos los miembros con estado activo.
     *
     * @return Lista de miembros activos
     */
    public List<Miembro> obtenerMiembrosActivos() {
        return miembroRepository.findMiembrosActivos();
    }

    /**
     * Obtiene miembros filtrados por tipo de plan de membresia.
     *
     * @param plan Nombre del plan a filtrar
     * @return Lista de miembros con el plan especificado
     */
    public List<Miembro> obtenerMiembrosPorPlan(String plan) {
        return miembroRepository.findByPlan(plan);
    }

    /**
     * Cuenta el numero de miembros con estado activo.
     *
     * @return Cantidad de miembros activos
     */
    public long contarMiembrosActivos() {
        return miembroRepository.findMiembrosActivos().size();
    }

    /**
     * Cuenta el numero total de miembros registrados.
     *
     * @return Cantidad total de miembros
     */
    public long contarTotalMiembros() {
        return miembroRepository.count();
    }

    /**
     * Renueva la membresia de un miembro extendiendola por un numero de meses.
     * Actualiza automaticamente la fecha de vencimiento.
     *
     * @param miembroId ID del miembro a renovar
     * @param meses Cantidad de meses a extender la membresia
     * @return Mensaje de exito o error segun corresponda
     */
    public String renovarMembresia(Long miembroId, int meses) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        miembro.renovarMembresia(meses);
        miembroRepository.save(miembro);

        return "SUCCESS: Membresía renovada por " + meses + " mes(es)";
    }

    /**
     * Verifica y actualiza automaticamente el estado de membresias vencidas.
     * Desactiva los miembros cuya fecha de vencimiento haya expirado.
     * Metodo util para ejecutar en tareas programadas.
     */
    public void verificarMembresiasVencidas() {
        List<Miembro> miembros = miembroRepository.findAll();

        for (Miembro miembro : miembros) {
            if (miembro.estaVencida() && miembro.getActivo()) {
                miembro.setActivo(false);
                miembroRepository.save(miembro);
            }
        }
    }

    /**
     * Cambia el estado de activacion de un miembro.
     *
     * @param miembroId ID del miembro
     * @param activo Nuevo estado (true para activo, false para inactivo)
     * @return Mensaje de exito o error segun corresponda
     */
    public String cambiarEstadoMiembro(Long miembroId, Boolean activo) {
        Miembro miembro = miembroRepository.findById(miembroId).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

        miembro.setActivo(activo);
        miembroRepository.save(miembro);

        return "SUCCESS: Estado actualizado";
    }

    /**
     * Actualiza el plan de membresia de un miembro.
     * Cambia tanto el nombre del plan como el detalle asociado.
     *
     * @param miembroId ID del miembro
     * @param nombrePlan Nombre del nuevo plan
     * @return Mensaje de exito o error segun corresponda
     */
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

    /**
     * Actualiza los datos personales de un miembro.
     * Valida que el nuevo email no este en uso por otro miembro.
     *
     * @param id ID del miembro a actualizar
     * @param nombre Nuevo nombre
     * @param email Nuevo email
     * @param telefono Nuevo telefono
     * @return Mensaje de exito o error segun corresponda
     */
    public String actualizarMiembro(Long id, String nombre, String email, String telefono) {
        Miembro miembro = miembroRepository.findById(id).orElse(null);

        if (miembro == null) {
            return "ERROR: Miembro no encontrado";
        }

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

    /**
     * Obtiene la lista de miembros cuya membresia vence en los proximos 7 dias.
     * Util para enviar recordatorios de renovacion.
     *
     * @return Lista de miembros proximos a vencer
     */
    public List<Miembro> obtenerMiembrosProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate dentroDe7Dias = hoy.plusDays(7);
        return miembroRepository.findByFechaVencimientoBetween(hoy, dentroDe7Dias);
    }

    /**
     * Cuenta la cantidad de miembros que tienen un plan especifico.
     *
     * @param plan Nombre del plan a contar
     * @return Cantidad de miembros con el plan especificado
     */
    public long contarMiembrosPorPlan(String plan) {
        return miembroRepository.findByPlan(plan).size();
    }
}