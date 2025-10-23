package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Administrador;
import com.example.ProyectoGym.Model.ClaseGrupal;
import com.example.ProyectoGym.Model.Instructor;
import com.example.ProyectoGym.Services.ClaseService;
import com.example.ProyectoGym.Services.InstructorService;
import com.example.ProyectoGym.Repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping
    public String mostrarClases(HttpSession session, Model model) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<ClaseGrupal> clases = claseService.obtenerTodasLasClases();
        List<Instructor> instructores = instructorService.obtenerTodosLosInstructores();

        // Agregar información de inscritos para cada clase
        List<Map<String, Object>> clasesConInfo = clases.stream().map(clase -> {
            Map<String, Object> info = new HashMap<>();
            info.put("id", clase.getId());
            info.put("nombre", clase.getNombre());
            info.put("descripcion", clase.getDescripcion());
            info.put("diaSemana", clase.getDiaSemana());
            info.put("horaInicio", clase.getHoraInicio());
            info.put("duracion", clase.getDuracion());
            info.put("capacidad", clase.getCapacidad());
            info.put("imagenUrl", clase.getImagenUrl());
            info.put("activa", clase.getActiva());
            info.put("instructor", clase.getInstructor() != null ?
                    clase.getInstructor().getNombre() : "Sin asignar");
            info.put("instructorId", clase.getInstructor() != null ?
                    clase.getInstructor().getId() : null);
            info.put("inscritos", reservaRepository.countReservasActivasByClase(clase));
            return info;
        }).toList();

        model.addAttribute("clases", clasesConInfo);
        model.addAttribute("instructores", instructores);

        return "clases-admin";
    }


    @GetMapping("/nueva")
    public String mostrarFormularioNueva(HttpSession session, Model model) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<Instructor> instructores = instructorService.obtenerInstructoresActivos();
        model.addAttribute("instructores", instructores);
        model.addAttribute("clase", new ClaseGrupal());
        model.addAttribute("titulo", "Nueva Clase");
        model.addAttribute("accion", "crear");
        return "clase-form";
    }


    @PostMapping("/crear")
    public String crearClase(@RequestParam("nombre") String nombre,
                             @RequestParam("descripcion") String descripcion,
                             @RequestParam("diaSemana") String diaSemana,
                             @RequestParam("horaInicio") String horaInicio,
                             @RequestParam("duracion") Integer duracion,
                             @RequestParam("capacidad") Integer capacidad,
                             @RequestParam("imagenUrl") String imagenUrl,
                             @RequestParam(value = "instructorId", required = false) Long instructorId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = claseService.crearClase(nombre, descripcion, diaSemana,
                horaInicio, duracion, capacidad, imagenUrl, instructorId);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Clase creada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/clases";
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, HttpSession session, Model model,
                                          RedirectAttributes redirectAttributes) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        ClaseGrupal clase = claseService.obtenerClasePorId(id);

        if (clase == null) {
            redirectAttributes.addFlashAttribute("mensaje", "Clase no encontrada");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/admin/clases";
        }

        List<Instructor> instructores = instructorService.obtenerInstructoresActivos();
        model.addAttribute("instructores", instructores);
        model.addAttribute("clase", clase);
        model.addAttribute("titulo", "Editar Clase");
        model.addAttribute("accion", "actualizar");

        return "clase-form";
    }


    @PostMapping("/actualizar/{id}")
    public String actualizarClase(@PathVariable Long id,
                                  @RequestParam("nombre") String nombre,
                                  @RequestParam("descripcion") String descripcion,
                                  @RequestParam("diaSemana") String diaSemana,
                                  @RequestParam("horaInicio") String horaInicio,
                                  @RequestParam("duracion") Integer duracion,
                                  @RequestParam("capacidad") Integer capacidad,
                                  @RequestParam("imagenUrl") String imagenUrl,
                                  @RequestParam(value = "instructorId", required = false) Long instructorId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        String resultado = claseService.actualizarClase(id, nombre, descripcion, diaSemana,
                horaInicio, duracion, capacidad, imagenUrl, instructorId);

        if (resultado.startsWith("SUCCESS")) {
            redirectAttributes.addFlashAttribute("mensaje", "Clase actualizada exitosamente");
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", resultado.replace("ERROR: ", ""));
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
        }

        return "redirect:/admin/clases";
    }


    @PostMapping("/cambiar-estado/{id}")
    @ResponseBody
    public Map<String, String> cambiarEstado(@PathVariable Long id, HttpSession session) {
        // ✅ VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        ClaseGrupal clase = claseService.obtenerClasePorId(id);

        if (clase == null) {
            return Map.of("status", "error", "message", "Clase no encontrada");
        }

        // Cambiar el estado (toggle)
        Boolean nuevoEstado = !clase.getActiva();
        String resultado = claseService.cambiarEstadoClase(id, nuevoEstado);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of(
                    "status", "success",
                    "message", nuevoEstado ? "Clase activada" : "Clase desactivada"
            );
        } else {
            return Map.of("status", "error", "message", resultado.replace("ERROR: ", ""));
        }
    }


    @PostMapping("/asignar-instructor")
    @ResponseBody
    public Map<String, String> asignarInstructor(@RequestParam Long claseId,
                                                 @RequestParam Long instructorId,
                                                 HttpSession session) {
        // VALIDAR SESIÓN
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        String resultado = claseService.asignarInstructor(claseId, instructorId);

        if (resultado.startsWith("SUCCESS")) {
            return Map.of("status", "success", "message", "Instructor asignado correctamente");
        } else {
            return Map.of("status", "error", "message", resultado.replace("ERROR: ", ""));
        }
    }
}