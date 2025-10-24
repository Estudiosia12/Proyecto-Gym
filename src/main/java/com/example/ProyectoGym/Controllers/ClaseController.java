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

/**
 * Controlador para la gestion de clases grupales desde el panel administrativo.
 * Maneja operaciones CRUD sobre clases, asignacion de instructores,
 * cambio de estado y visualizacion de informacion de inscritos.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
@RequestMapping("/admin/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @Autowired
    private InstructorService instructorService;

    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Muestra la lista completa de clases grupales con informacion detallada.
     * Incluye datos del instructor asignado y cantidad de miembros inscritos.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista con lista de clases o redireccion al login
     */
    @GetMapping
    public String mostrarClases(HttpSession session, Model model) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        List<ClaseGrupal> clases = claseService.obtenerTodasLasClases();
        List<Instructor> instructores = instructorService.obtenerTodosLosInstructores();

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

    /**
     * Muestra el formulario para crear una nueva clase grupal.
     *
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @return Vista del formulario de clase o redireccion al login
     */
    @GetMapping("/nueva")
    public String mostrarFormularioNueva(HttpSession session, Model model) {
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

    /**
     * Procesa la creacion de una nueva clase grupal.
     * Valida los datos y asigna un instructor si se especifica.
     *
     * @param nombre Nombre de la clase
     * @param descripcion Descripcion detallada de la clase
     * @param diaSemana Dia de la semana en que se realiza
     * @param horaInicio Hora de inicio en formato HH:mm
     * @param duracion Duracion en minutos
     * @param capacidad Capacidad maxima de participantes
     * @param imagenUrl URL de la imagen representativa
     * @param instructorId ID del instructor a asignar (opcional)
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de clases con mensaje de resultado
     */
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

    /**
     * Muestra el formulario para editar una clase grupal existente.
     *
     * @param id ID de la clase a editar
     * @param session Sesion HTTP para validar autenticacion
     * @param model Modelo para pasar datos a la vista
     * @param redirectAttributes Atributos para mensajes flash si hay error
     * @return Vista del formulario con datos de la clase o redireccion si no existe
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, HttpSession session, Model model,
                                          RedirectAttributes redirectAttributes) {
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

    /**
     * Procesa la actualizacion de una clase grupal existente.
     * Permite modificar todos los datos de la clase incluyendo el instructor asignado.
     *
     * @param id ID de la clase a actualizar
     * @param nombre Nuevo nombre de la clase
     * @param descripcion Nueva descripcion
     * @param diaSemana Nuevo dia de la semana
     * @param horaInicio Nueva hora de inicio
     * @param duracion Nueva duracion
     * @param capacidad Nueva capacidad
     * @param imagenUrl Nueva URL de imagen
     * @param instructorId Nuevo ID de instructor
     * @param session Sesion HTTP para validar autenticacion
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion a la lista de clases con mensaje de resultado
     */
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

    /**
     * Cambia el estado de una clase entre activa e inactiva mediante peticion AJAX.
     * Realiza un toggle del estado actual de la clase.
     *
     * @param id ID de la clase a modificar
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion y mensaje descriptivo
     */
    @PostMapping("/cambiar-estado/{id}")
    @ResponseBody
    public Map<String, String> cambiarEstado(@PathVariable Long id, HttpSession session) {
        Administrador admin = (Administrador) session.getAttribute("administrador");
        if (admin == null) {
            return Map.of("status", "error", "message", "Sesión expirada. Por favor inicia sesión.");
        }

        ClaseGrupal clase = claseService.obtenerClasePorId(id);

        if (clase == null) {
            return Map.of("status", "error", "message", "Clase no encontrada");
        }

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

    /**
     * Asigna un instructor a una clase grupal mediante peticion AJAX.
     *
     * @param claseId ID de la clase a la cual asignar el instructor
     * @param instructorId ID del instructor a asignar
     * @param session Sesion HTTP para validar autenticacion
     * @return JSON con estado de la operacion y mensaje descriptivo
     */
    @PostMapping("/asignar-instructor")
    @ResponseBody
    public Map<String, String> asignarInstructor(@RequestParam Long claseId,
                                                 @RequestParam Long instructorId,
                                                 HttpSession session) {
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