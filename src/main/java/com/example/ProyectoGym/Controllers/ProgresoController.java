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

/**
 * Controlador para la gestion del progreso de entrenamiento desde el panel administrativo.
 * Maneja la visualizacion de miembros con rutinas, seguimiento de sesiones completadas,
 * registro manual de sesiones y estadisticas generales de progreso.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
@RequestMapping("/admin/progreso")
public class ProgresoController {

    @Autowired
    private ProgresoService progresoService;

    /**
     * Muestra la pagina principal de seguimiento de progreso de miembros.
     * Incluye lista de miembros con rutinas asignadas, porcentaje de cumplimiento
     * y estadisticas generales del gimnasio.
     *
     * @param session Sesion HTTP para validar autenticacion del administrador
     * @param model Modelo para pasar datos a la vista
     * @return Vista de progreso general o redireccion al login
     */
    @GetMapping
    public String mostrarProgreso(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<Map<String, Object>> miembros = progresoService.obtenerMiembrosConRutinas();
        Map<String, Object> stats = progresoService.obtenerEstadisticasGenerales();

        model.addAttribute("miembros", miembros);
        model.addAttribute("stats", stats);

        return "progreso-admin";
    }

    /**
     * Muestra el detalle completo del progreso de un miembro especifico.
     * Incluye informacion de rutina asignada, estadisticas de sesiones,
     * porcentaje de progreso mensual y historial de las ultimas 10 sesiones.
     *
     * @param miembroId ID del miembro a consultar
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash si hay error
     * @return Vista de detalle de progreso o redireccion si el miembro no tiene rutina
     */
    @GetMapping("/detalle/{miembroId}")
    public String mostrarDetalleProgreso(@PathVariable Long miembroId, HttpSession session, Model model,
                                         RedirectAttributes redirectAttributes) {
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

    /**
     * Registra manualmente la completacion de una sesion de entrenamiento mediante peticion AJAX.
     * Permite al administrador marcar sesiones para miembros que completaron su rutina.
     *
     * @param miembroId ID del miembro que completo la sesion
     * @param observaciones Notas u observaciones sobre la sesion (opcional)
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion y mensaje descriptivo
     */
    @PostMapping("/marcar-sesion/{miembroId}")
    @ResponseBody
    public Map<String, String> marcarSesion(@PathVariable Long miembroId,
                                            @RequestParam(required = false) String observaciones,
                                            HttpSession session) {
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

    /**
     * Elimina una sesion completada del sistema mediante peticion AJAX.
     * Util para corregir registros erroneos o duplicados.
     *
     * @param sesionId ID de la sesion a eliminar
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion y mensaje descriptivo
     */
    @PostMapping("/eliminar-sesion/{sesionId}")
    @ResponseBody
    public Map<String, String> eliminarSesion(@PathVariable Long sesionId, HttpSession session) {
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