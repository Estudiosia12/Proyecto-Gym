package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Services.ProgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/progreso")
public class ProgresoController {

    @Autowired
    private ProgresoService progresoService;


    @GetMapping
    public String mostrarProgreso(HttpSession session, Model model) {
        // Validar
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Obtener lista de miembros con rutinas
        List<Map<String, Object>> miembros = progresoService.obtenerMiembrosConRutinas();

        // Estadísticas generales
        Map<String, Object> stats = progresoService.obtenerEstadisticasGenerales();

        model.addAttribute("miembros", miembros);
        model.addAttribute("stats", stats);

        return "progreso-admin";
    }


    @GetMapping("/detalle/{miembroId}")
    public String mostrarDetalleProgreso(@PathVariable Long miembroId, HttpSession session, Model model,
                                         RedirectAttributes redirectAttributes) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Map<String, Object> detalle = progresoService.obtenerDetalleProgreso(miembroId);

        if (detalle == null) {
            redirectAttributes.addFlashAttribute("mensaje",
                    "El miembro no tiene una rutina asignada");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/progreso";
        }

        model.addAttribute("detalle", detalle);
        return "progreso-detalle";
    }


    @PostMapping("/marcar-sesion/{miembroId}")
    @ResponseBody
    public Map<String, String> marcarSesion(@PathVariable Long miembroId,
                                            @RequestParam(required = false) String observaciones,
                                            HttpSession session) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        String resultado = progresoService.marcarSesionCompletada(miembroId, observaciones);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of(
                    "status", "success",
                    "message", "Sesión completada registrada correctamente"
            );
        } else {
            return Map.of(
                    "status", "error",
                    "message", resultado.replace("ERROR: ", "")
            );
        }
    }


    @PostMapping("/eliminar-sesion/{sesionId}")
    @ResponseBody
    public Map<String, String> eliminarSesion(@PathVariable Long sesionId, HttpSession session) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        String resultado = progresoService.eliminarSesion(sesionId);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of("status", "success", "message", "Sesión eliminada correctamente");
        } else {
            return Map.of("status", "error", "message", resultado.replace("ERROR: ", ""));
        }
    }
}