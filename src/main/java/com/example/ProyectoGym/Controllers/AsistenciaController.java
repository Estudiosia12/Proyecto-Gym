package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Services.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestion de asistencias al gimnasio desde el panel administrativo.
 * Maneja el registro de entradas y salidas, visualizacion de estado actual
 * y consulta de historial de asistencias.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
@RequestMapping("/admin/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    /**
     * Muestra la pagina principal de gestion de asistencias.
     * Incluye lista de miembros con su estado actual, historial del dia
     * y estadisticas de asistencias y presencia.
     *
     * @param session Sesion HTTP para validar autenticacion del administrador
     * @param model Modelo para pasar datos a la vista
     * @return Vista de asistencias o redireccion al login si no hay sesion activa
     */
    @GetMapping
    public String mostrarAsistencias(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<Map<String, Object>> miembrosConEstado = asistenciaService.obtenerListaMiembrosConEstado();
        List<Map<String, Object>> historialHoy = asistenciaService.obtenerHistorialHoy();
        long asistenciasHoy = asistenciaService.contarAsistenciasHoy();
        long miembrosEnGimnasio = asistenciaService.contarMiembrosEnGimnasio();

        model.addAttribute("miembros", miembrosConEstado);
        model.addAttribute("historial", historialHoy);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("miembrosEnGimnasio", miembrosEnGimnasio);

        return "asistencias-admin";
    }

    /**
     * Registra la entrada de un miembro al gimnasio mediante peticion AJAX.
     * Valida que exista sesion de administrador activa antes de procesar.
     *
     * @param miembroId ID del miembro que ingresa
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion (success/error) y mensaje descriptivo
     */
    @PostMapping("/entrada/{miembroId}")
    @ResponseBody
    public Map<String, String> registrarEntrada(@PathVariable Long miembroId, HttpSession session) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        String resultado = asistenciaService.registrarEntrada(miembroId);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of("status", "success", "message", resultado.replace("SUCCESS: ", ""));
        } else {
            return Map.of("status", "error", "message", resultado.replace("ERROR: ", ""));
        }
    }

    /**
     * Registra la salida de un miembro del gimnasio mediante peticion AJAX.
     * Valida que exista sesion de administrador activa antes de procesar.
     *
     * @param miembroId ID del miembro que sale
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion (success/error) y mensaje con duracion de visita
     */
    @PostMapping("/salida/{miembroId}")
    @ResponseBody
    public Map<String, String> registrarSalida(@PathVariable Long miembroId, HttpSession session) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        String resultado = asistenciaService.registrarSalida(miembroId);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of("status", "success", "message", resultado.replace("SUCCESS: ", ""));
        } else {
            return Map.of("status", "error", "message", resultado.replace("ERROR: ", ""));
        }
    }

    /**
     * Verifica el estado actual de asistencia de un miembro mediante peticion AJAX.
     * Retorna si esta presente en el gimnasio y sus asistencias del mes.
     *
     * @param miembroId ID del miembro a consultar
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de presencia y cantidad de asistencias mensuales
     */
    @GetMapping("/estado/{miembroId}")
    @ResponseBody
    public Map<String, Object> verificarEstado(@PathVariable Long miembroId, HttpSession session) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("error", "Sesión expirada");
        }

        boolean enGimnasio = asistenciaService.miembroEstaEnGimnasio(miembroId);
        long asistenciasMes = asistenciaService.contarAsistenciasMiembroMes(miembroId);

        return Map.of(
                "enGimnasio", enGimnasio,
                "asistenciasMes", asistenciasMes
        );
    }

    /**
     * Muestra el historial completo de asistencias de un miembro especifico.
     *
     * @param miembroId ID del miembro a consultar
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista con historial de asistencias o redireccion al login
     */
    @GetMapping("/historial/{miembroId}")
    public String verHistorialMiembro(@PathVariable Long miembroId, HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        var asistencias = asistenciaService.obtenerAsistenciasPorMiembro(miembroId);
        model.addAttribute("asistencias", asistencias);
        return "historial-asistencias";
    }
}