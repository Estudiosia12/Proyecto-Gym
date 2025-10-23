package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Model.ClaseGrupal;
import com.example.ProyectoGym.Model.AsignacionRutina;
import com.example.ProyectoGym.Model.SesionCompletada;
import com.example.ProyectoGym.Services.MiembroService;
import com.example.ProyectoGym.Services.ClaseService;
import com.example.ProyectoGym.Services.RutinaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/miembro")
public class MiembroController {

    @Autowired
    private MiembroService miembroService;

    @Autowired
    private ClaseService claseService;

    @Autowired
    private RutinaService rutinaService;


    // Dashboard del miembro
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        model.addAttribute("miembro", miembro);

        // Obtener reservas activas para mostrar en el dashboard
        model.addAttribute("reservas", claseService.obtenerReservasActivas(miembro));

        return "dashboard";
    }


    // Perfil del miembro
    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        // Calcular edad
        if (miembro.getFecha_nacimiento() != null) {
            int edad = LocalDate.now().getYear() - miembro.getFecha_nacimiento().getYear();
            model.addAttribute("edad", edad);
        }

        model.addAttribute("miembro", miembro);
        return "perfil";
    }


    // Ver clases disponibles
    @GetMapping("/clases")
    public String clases(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        // Obtener todas las clases activas
        List<ClaseGrupal> clases = claseService.obtenerClasesActivas();
        model.addAttribute("clases", clases);

        // Calcular cupos disponibles para cada clase
        Map<Long, Integer> cuposDisponibles = new HashMap<>();
        for (ClaseGrupal clase : clases) {
            cuposDisponibles.put(clase.getId(), claseService.calcularCuposDisponibles(clase));
        }
        model.addAttribute("cuposDisponibles", cuposDisponibles);

        // Obtener reservas activas del miembro
        model.addAttribute("reservas", claseService.obtenerReservasActivas(miembro));

        // Verificar si puede reservar (es Premium)
        model.addAttribute("puedeReservar", claseService.puedeReservar(miembro));

        model.addAttribute("miembro", miembro);
        return "clases";
    }

    // Reservar una clase
    @PostMapping("/clases/reservar")
    public String reservarClase(@RequestParam Long claseId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        String resultado = claseService.reservarClase(miembro, claseId);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("SUCCESS: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/miembro/clases";
    }

    // Cancelar reserva
    @PostMapping("/clases/cancelar")
    public String cancelarReserva(@RequestParam Long reservaId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        String resultado = claseService.cancelarReserva(reservaId, miembro);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("SUCCESS: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/miembro/clases";
    }


    // Ver página de rutinas
    @GetMapping("/rutinas")
    public String rutinas(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        // Verificar si tiene rutina asignada
        boolean tieneRutina = rutinaService.tieneRutinaAsignada(miembro);
        model.addAttribute("tieneRutina", tieneRutina);

        if (tieneRutina) {
            // Obtener rutina asignada
            AsignacionRutina asignacion = rutinaService.obtenerRutinaAsignada(miembro);
            model.addAttribute("asignacion", asignacion);

            // Obtener ejercicios
            model.addAttribute("ejercicios", rutinaService.obtenerEjerciciosDeRutina(asignacion.getRutinaPredefinida()));

            // Estadísticas
            model.addAttribute("sesionesEsteMes", rutinaService.contarSesionesEsteMes(miembro));
            model.addAttribute("porcentajeProgreso", rutinaService.calcularPorcentajeProgreso(miembro));
            model.addAttribute("sesionesRestantes", rutinaService.calcularSesionesRestantes(miembro));
        }

        model.addAttribute("miembro", miembro);
        return "rutinas";
    }

    // Mostrar formulario de selección de objetivo
    @GetMapping("/rutinas/seleccionar-objetivo")
    public String seleccionarObjetivo(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        model.addAttribute("miembro", miembro);
        return "seleccionar-objetivo";
    }

    // Mostrar formulario de selección de nivel
    @GetMapping("/rutinas/seleccionar-nivel")
    public String seleccionarNivel(@RequestParam String objetivo, HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        model.addAttribute("objetivo", objetivo);
        model.addAttribute("miembro", miembro);
        return "seleccionar-nivel";
    }

    // Asignar rutina al miembro
    @PostMapping("/rutinas/asignar")
    public String asignarRutina(@RequestParam String objetivo,
                                @RequestParam String nivel,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        String resultado = rutinaService.asignarRutina(miembro, objetivo, nivel);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("SUCCESS: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/miembro/rutinas";
    }

    // Cancelar rutina actual para seleccionar otra
    @PostMapping("/rutinas/cancelar")
    public String cancelarRutina(HttpSession session, RedirectAttributes redirectAttributes) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        String resultado = rutinaService.cancelarRutinaActual(miembro);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Rutina cancelada. Ahora puedes seleccionar una nueva.");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/miembro/rutinas";
    }


    // Ver progreso
    @GetMapping("/progreso")
    public String progreso(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        // Verificar si tiene rutina asignada
        boolean tieneRutina = rutinaService.tieneRutinaAsignada(miembro);
        model.addAttribute("tieneRutina", tieneRutina);

        if (tieneRutina) {
            AsignacionRutina asignacion = rutinaService.obtenerRutinaAsignada(miembro);
            model.addAttribute("asignacion", asignacion);

            // Métricas
            model.addAttribute("sesionesTotales", rutinaService.contarSesionesTotales(miembro));
            model.addAttribute("sesionesEsteMes", rutinaService.contarSesionesEsteMes(miembro));
            model.addAttribute("porcentajeAsistencia", rutinaService.calcularPorcentajeAsistencia(miembro));
            model.addAttribute("diferenciaMesAnterior", rutinaService.calcularDiferenciaMesAnterior(miembro));
            model.addAttribute("porcentajeProgreso", rutinaService.calcularPorcentajeProgreso(miembro));
            model.addAttribute("sesionesRestantes", rutinaService.calcularSesionesRestantes(miembro));

            // Historial
            model.addAttribute("ultimasSesiones", rutinaService.obtenerUltimasSesiones(miembro, 10));

            // Próxima sesión
            model.addAttribute("proximaSesion", rutinaService.calcularProximaSesion(miembro));

            // Última sesión
            SesionCompletada ultimaSesion = rutinaService.obtenerUltimaSesion(miembro);
            model.addAttribute("ultimaSesion", ultimaSesion);
        }

        model.addAttribute("miembro", miembro);
        return "progreso";
    }
}