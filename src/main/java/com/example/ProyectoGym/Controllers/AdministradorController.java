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


    // Mostrar formulario de login
    @GetMapping("/login")
    public String mostrarLogin() {
        return "admin-login";
    }

    // Procesar login
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String usuario,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Administrador admin = administradorService.autenticarAdministrador(usuario, password);

        if (admin != null) {
            // Guardar admin en sesión
            session.setAttribute("administrador", admin);
            return "redirect:/admin/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario o contraseña incorrectos");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/login";
        }
    }

    // Cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }

    // Dashboard del administrador
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");

        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Obtener todas las métricas del dashboard
        Map<String, Object> metricas = adminService.obtenerMetricasDashboard();

        // Pasar métricas al modelo
        model.addAttribute("administrador", admin);
        model.addAttribute("metricas", metricas);

        // Pasar métricas individuales para fácil acceso en la vista
        model.addAttribute("miembrosActivos", metricas.get("miembrosActivos"));
        model.addAttribute("membresiasActivas", metricas.get("membresiasActivas"));
        model.addAttribute("asistenciasHoy", metricas.get("asistenciasHoy"));
        model.addAttribute("ingresosMes", metricas.get("ingresosMes"));
        model.addAttribute("distribucionPlanes", metricas.get("distribucionPlanes"));
        model.addAttribute("porcentajesPlanes", metricas.get("porcentajesPlanes"));
        model.addAttribute("resumenClases", metricas.get("resumenClases"));

        return "dashboard-admin";
    }


    // Ver lista de miembros
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

    // Cambiar estado de miembro (Activo/Inactivo)
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


    // Ver lista de planes
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

    // Mostrar formulario para crear plan
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

    // Crear o actualizar plan
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

    // Mostrar formulario para editar plan
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

    // Eliminar plan
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

    // Cambiar estado del plan (Activo/Inactivo)
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