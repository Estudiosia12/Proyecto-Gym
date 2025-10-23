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

@Controller
@RequestMapping("/admin/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @GetMapping
    public String mostrarAsistencias(HttpSession session, Model model) {
        // Validar sesion
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Obtener lista de miembros con su estado
        List<Map<String, Object>> miembrosConEstado = asistenciaService.obtenerListaMiembrosConEstado();

        // Obtener historial de hoy
        List<Map<String, Object>> historialHoy = asistenciaService.obtenerHistorialHoy();

        // Estadísticas
        long asistenciasHoy = asistenciaService.contarAsistenciasHoy();
        long miembrosEnGimnasio = asistenciaService.contarMiembrosEnGimnasio();

        model.addAttribute("miembros", miembrosConEstado);
        model.addAttribute("historial", historialHoy);
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("miembrosEnGimnasio", miembrosEnGimnasio);

        return "asistencias-admin";
    }

    @PostMapping("/entrada/{miembroId}")
    @ResponseBody
    public Map<String, String> registrarEntrada(@PathVariable Long miembroId, HttpSession session) {
        // ✅ VALIDAR SESIÓN
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

    @PostMapping("/salida/{miembroId}")
    @ResponseBody
    public Map<String, String> registrarSalida(@PathVariable Long miembroId, HttpSession session) {
        // ✅ VALIDAR SESIÓN
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


    @GetMapping("/estado/{miembroId}")
    @ResponseBody
    public Map<String, Object> verificarEstado(@PathVariable Long miembroId, HttpSession session) {
        // ✅ VALIDAR SESIÓN
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

    @GetMapping("/historial/{miembroId}")
    public String verHistorialMiembro(@PathVariable Long miembroId, HttpSession session, Model model) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        var asistencias = asistenciaService.obtenerAsistenciasPorMiembro(miembroId);
        model.addAttribute("asistencias", asistencias);
        return "historial-asistencias";
    }
}