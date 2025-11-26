package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Services.AdministradorService;
import com.example.ProyectoGym.Services.AdminService;
import com.example.ProyectoGym.Services.MiembroService;
import com.example.ProyectoGym.Services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

/**
 * Controlador principal para la gestion administrativa del gimnasio.
 * Maneja autenticacion de administradores, dashboard con metricas,
 * y operaciones CRUD sobre miembros y planes de membresia.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
@RequestMapping("/admin")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private MiembroService miembroService;

    @Autowired
    private PlanService planService;

    /**
     * Muestra el formulario de inicio de sesion para administradores.
     *
     * @return Vista del formulario de login
     */
    @GetMapping("/login")
    public String mostrarLogin() {
        return "admin-login";
    }

    /**
     * Procesa el inicio de sesion del administrador.
     * Valida credenciales y crea sesion si son correctas.
     *
     * @param usuario Nombre de usuario del administrador
     * @param password Contrasena del administrador
     * @param session Sesion HTTP para almacenar datos del administrador autenticado
     * @param redirectAttributes Atributos para mensajes flash en redirecciones
     * @return Redireccion al dashboard si autenticacion exitosa, o al login con mensaje de error
     */
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Administrador admin = administradorService.autenticarAdministrador(usuario, password);

        if (admin != null) {
            session.setAttribute("administrador", admin);
            return "redirect:/admin/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario o contraseña incorrectos");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/login";
        }
    }

    /**
     * Cierra la sesion del administrador y limpia todos los datos de sesion.
     *
     * @param session Sesion HTTP  invalidar
     * @return Redireccion al formulario de login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    /**
     * Muestra el dashboard principal del administrador con metricas y estadisticas.
     * Incluye miembros activos, asistencias, ingresos, distribucion de planes y resumen de clases.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista del dashboard o redireccion al login si no hay sesion activa
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        Map<String, Object> metricas = adminService.obtenerMetricasDashboard();

        model.addAttribute("administrador", admin);
        model.addAttribute("metricas", metricas);
        model.addAttribute("miembrosActivos", metricas.get("miembrosActivos"));
        model.addAttribute("membresiasActivas", metricas.get("membresiasActivas"));
        model.addAttribute("asistenciasHoy", metricas.get("asistenciasHoy"));
        model.addAttribute("ingresosMes", metricas.get("ingresosMes"));
        model.addAttribute("distribucionPlanes", metricas.get("distribucionPlanes"));
        model.addAttribute("porcentajesPlanes", metricas.get("porcentajesPlanes"));
        model.addAttribute("resumenClases", metricas.get("resumenClases"));

        return "dashboard-admin";
    }

    /**
     * Muestra la lista completa de miembros registrados en el gimnasio.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista con lista de miembros o redireccion al login
     */
    @GetMapping("/miembros")
    public String listarMiembros(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("administrador", admin);
        model.addAttribute("miembros", miembroService.obtenerTodosLosMiembros());

        return "miembros-admin";
    }

    /**
     * Cambia el estado de un miembro entre activo e inactivo.
     *
     * @param miembroId ID del miembro a modificar
     * @param nuevoEstado Nuevo estado del miembro (true para activo, false para inactivo)
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de miembros con mensaje de resultado
     */
    @PostMapping("/miembros/cambiar-estado")
    public String cambiarEstadoMiembro(@RequestParam Long miembroId,
                                       @RequestParam Boolean nuevoEstado,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = miembroService.cambiarEstadoMiembro(miembroId, nuevoEstado);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Estado del miembro actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/miembros";
    }

    /**
     * Muestra la lista completa de planes de membresia del gimnasio.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista con lista de planes o redireccion al login
     */
    @GetMapping("/planes")
    public String listarPlanes(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("administrador", admin);
        model.addAttribute("planes", planService.obtenerTodosLosPlanes());

        return "planes-admin";
    }

    /**
     * Muestra el formulario para crear un nuevo plan de membresia.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista del formulario de plan o redireccion al login
     */
    @GetMapping("/planes/nuevo")
    public String mostrarFormularioNuevoPlan(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        model.addAttribute("administrador", admin);
        model.addAttribute("plan", new Plan());

        return "plan-form";
    }

    /**
     * Procesa la creacion o actualizacion de un plan de membresia.
     *
     * @param plan Objeto plan con los datos del formulario
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de planes con mensaje de resultado
     */
    @PostMapping("/planes/guardar")
    public String guardarPlan(@ModelAttribute Plan plan,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = planService.guardarPlan(plan);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Plan guardado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/planes";
    }

    /**
     * Muestra el formulario para editar un plan de membresia existente.
     *
     * @param id ID del plan a editar
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash si hay error
     * @return Vista del formulario con datos del plan o redireccion si no existe
     */
    @GetMapping("/planes/editar/{id}")
    public String mostrarFormularioEditarPlan(@PathVariable Long id,
                                              HttpSession session,
                                              Model model,
                                              RedirectAttributes redirectAttributes) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        Plan plan = planService.obtenerPlanPorId(id);

        if (plan == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Plan no encontrado");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/planes";
        }

        model.addAttribute("administrador", admin);
        model.addAttribute("plan", plan);

        return "plan-form";
    }

    /**
     * Elimina logicamente un plan de membresia desactivandolo.
     *
     * @param id ID del plan a eliminar
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de planes con mensaje de resultado
     */
    @PostMapping("/planes/eliminar/{id}")
    public String eliminarPlan(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = planService.eliminarPlan(id);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Plan desactivado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/planes";
    }

    /**
     * Cambia el estado de un plan entre activo e inactivo.
     *
     * @param planId ID del plan a modificar
     * @param nuevoEstado Nuevo estado del plan (true para activo, false para inactivo)
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de planes con mensaje de resultado
     */
    @PostMapping("/planes/cambiar-estado")
    public String cambiarEstadoPlan(@RequestParam Long planId,
                                    @RequestParam Boolean nuevoEstado,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = planService.cambiarEstadoPlan(planId, nuevoEstado);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Estado del plan actualizado exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/planes";
    }
}