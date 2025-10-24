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

/**
 * Controlador para la gestion del panel de miembros del gimnasio.
 * Maneja dashboard personalizado, perfil, reservas de clases grupales,
 * asignacion y seguimiento de rutinas, y visualizacion de progreso de entrenamiento.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
@RequestMapping("/miembro")
public class MiembroController {

    @Autowired
    private MiembroService miembroService;

    @Autowired
    private ClaseService claseService;

    @Autowired
    private RutinaService rutinaService;

    /**
     * Muestra el dashboard principal del miembro con resumen de actividad.
     * Incluye informacion de membresia y reservas de clases activas.
     *
     * @param session Sesion HTTP para validar autenticacion del miembro
     * @param model Modelo para pasar datos a la vista
     * @return Vista del dashboard o redireccion al login si no hay sesion activa
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        model.addAttribute("miembro", miembro);
        model.addAttribute("reservas", claseService.obtenerReservasActivas(miembro));

        return "dashboard";
    }

    /**
     * Muestra el perfil completo del miembro con sus datos personales.
     * Calcula automaticamente la edad basandose en la fecha de nacimiento.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista del perfil o redireccion al login
     */
    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        if (miembro.getFecha_nacimiento() != null) {
            int edad = LocalDate.now().getYear() - miembro.getFecha_nacimiento().getYear();
            model.addAttribute("edad", edad);
        }

        model.addAttribute("miembro", miembro);
        return "perfil";
    }

    /**
     * Muestra las clases grupales disponibles con informacion de cupos.
     * Incluye reservas activas del miembro y valida si puede reservar segun su plan.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista de clases disponibles o redireccion al login
     */
    @GetMapping("/clases")
    public String clases(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        List<ClaseGrupal> clases = claseService.obtenerClasesActivas();
        model.addAttribute("clases", clases);

        Map<Long, Integer> cuposDisponibles = new HashMap<>();
        for (ClaseGrupal clase : clases) {
            cuposDisponibles.put(clase.getId(), claseService.calcularCuposDisponibles(clase));
        }
        model.addAttribute("cuposDisponibles", cuposDisponibles);

        model.addAttribute("reservas", claseService.obtenerReservasActivas(miembro));
        model.addAttribute("puedeReservar", claseService.puedeReservar(miembro));
        model.addAttribute("miembro", miembro);

        return "clases";
    }

    /**
     * Procesa la reserva de una clase grupal por parte del miembro.
     * Valida disponibilidad de cupos y permisos segun el plan de membresia.
     *
     * @param claseId ID de la clase a reservar
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de clases con mensaje de resultado
     */
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

    /**
     * Procesa la cancelacion de una reserva de clase grupal.
     *
     * @param reservaId ID de la reserva a cancelar
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de clases con mensaje de resultado
     */
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

    /**
     * Muestra la rutina de entrenamiento asignada al miembro.
     * Incluye ejercicios detallados, estadisticas de sesiones y progreso mensual.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista de rutinas o redireccion al login
     */
    @GetMapping("/rutinas")
    public String rutinas(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        boolean tieneRutina = rutinaService.tieneRutinaAsignada(miembro);
        model.addAttribute("tieneRutina", tieneRutina);

        if (tieneRutina) {
            AsignacionRutina asignacion = rutinaService.obtenerRutinaAsignada(miembro);
            model.addAttribute("asignacion", asignacion);
            model.addAttribute("ejercicios", rutinaService.obtenerEjerciciosDeRutina(asignacion.getRutinaPredefinida()));
            model.addAttribute("sesionesEsteMes", rutinaService.contarSesionesEsteMes(miembro));
            model.addAttribute("porcentajeProgreso", rutinaService.calcularPorcentajeProgreso(miembro));
            model.addAttribute("sesionesRestantes", rutinaService.calcularSesionesRestantes(miembro));
        }

        model.addAttribute("miembro", miembro);
        return "rutinas";
    }

    /**
     * Muestra el formulario de seleccion de objetivo de entrenamiento.
     * Primer paso en el proceso de asignacion de rutina personalizada.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista de seleccion de objetivo o redireccion al login
     */
    @GetMapping("/rutinas/seleccionar-objetivo")
    public String seleccionarObjetivo(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        model.addAttribute("miembro", miembro);
        return "seleccionar-objetivo";
    }

    /**
     * Muestra el formulario de seleccion de nivel de dificultad.
     * Segundo paso en el proceso de asignacion de rutina, filtrado por objetivo.
     *
     * @param objetivo Objetivo de entrenamiento seleccionado previamente
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista de seleccion de nivel o redireccion al login
     */
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

    /**
     * Procesa la asignacion de una rutina al miembro segun objetivo y nivel seleccionados.
     * Busca la rutina predefinida correspondiente y la asigna al miembro.
     *
     * @param objetivo Objetivo de entrenamiento seleccionado
     * @param nivel Nivel de dificultad seleccionado
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la vista de rutinas con mensaje de resultado
     */
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

    /**
     * Cancela la rutina actual del miembro permitiendo seleccionar una nueva.
     * Desactiva la asignacion de rutina existente.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la vista de rutinas con mensaje de confirmacion
     */
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

    /**
     * Muestra el panel de progreso de entrenamiento del miembro.
     * Incluye metricas detalladas, historial de sesiones, comparativas mensuales
     * y recomendaciones de proxima sesion.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista de progreso o redireccion al login
     */
    @GetMapping("/progreso")
    public String progreso(HttpSession session, Model model) {
        Miembro miembro = (Miembro) session.getAttribute("miembro");

        if (miembro == null) {
            return "redirect:/login";
        }

        boolean tieneRutina = rutinaService.tieneRutinaAsignada(miembro);
        model.addAttribute("tieneRutina", tieneRutina);

        if (tieneRutina) {
            AsignacionRutina asignacion = rutinaService.obtenerRutinaAsignada(miembro);
            model.addAttribute("asignacion", asignacion);
            model.addAttribute("sesionesTotales", rutinaService.contarSesionesTotales(miembro));
            model.addAttribute("sesionesEsteMes", rutinaService.contarSesionesEsteMes(miembro));
            model.addAttribute("porcentajeAsistencia", rutinaService.calcularPorcentajeAsistencia(miembro));
            model.addAttribute("diferenciaMesAnterior", rutinaService.calcularDiferenciaMesAnterior(miembro));
            model.addAttribute("porcentajeProgreso", rutinaService.calcularPorcentajeProgreso(miembro));
            model.addAttribute("sesionesRestantes", rutinaService.calcularSesionesRestantes(miembro));
            model.addAttribute("ultimasSesiones", rutinaService.obtenerUltimasSesiones(miembro, 10));
            model.addAttribute("proximaSesion", rutinaService.calcularProximaSesion(miembro));

            SesionCompletada ultimaSesion = rutinaService.obtenerUltimaSesion(miembro);
            model.addAttribute("ultimaSesion", ultimaSesion);
        }

        model.addAttribute("miembro", miembro);
        return "progreso";
    }
}