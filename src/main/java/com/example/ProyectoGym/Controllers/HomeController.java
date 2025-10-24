package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Plan;
import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Services.PlanService;
import com.example.ProyectoGym.Services.MiembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador principal para las paginas publicas del gimnasio.
 * Maneja la pagina de inicio, registro de nuevos miembros, autenticacion
 * y paginas informativas como Nosotros y FAQ.
 *
 * @author Juan Quispe, Pedro Perez
 * @since 2025
 */
@Controller
public class HomeController {

    @Autowired
    private PlanService planService;

    @Autowired
    private MiembroService miembroService;

    /**
     * Muestra la pagina de inicio del gimnasio con informacion de planes.
     * Obtiene y muestra los planes Basico y Premium activos.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Vista de la pagina principal
     */
    @GetMapping("/")
    public String mostrarHome(Model model) {
        List<Plan> planes = planService.obtenerPlanesActivos();

        Plan planBasico = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Basico"))
                .findFirst()
                .orElse(null);

        Plan planPremium = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Premium"))
                .findFirst()
                .orElse(null);

        model.addAttribute("planBasico", planBasico);
        model.addAttribute("planPremium", planPremium);

        return "home";
    }

    /**
     * Muestra la pagina informativa "Acerca de Nosotros".
     *
     * @return Vista de la pagina Nosotros
     */
    @GetMapping("/nosotros")
    public String mostrarNosotros() {
        return "nosotros";
    }

    /**
     * Muestra la pagina de preguntas frecuentes (FAQ).
     *
     * @return Vista de la pagina FAQ
     */
    @GetMapping("/faq")
    public String mostrarFaq() {
        return "faq";
    }

    /**
     * Muestra el formulario de registro de nuevos miembros.
     * Incluye informacion de los planes Basico y Premium disponibles.
     *
     * @param model Modelo para pasar datos a la vista
     * @return Vista del formulario de registro
     */
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        List<Plan> planes = planService.obtenerPlanesActivos();

        Plan planBasico = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Basico"))
                .findFirst()
                .orElse(null);

        Plan planPremium = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Premium"))
                .findFirst()
                .orElse(null);

        model.addAttribute("planBasico", planBasico);
        model.addAttribute("planPremium", planPremium);

        return "registro";
    }

    /**
     * Procesa el registro de un nuevo miembro en el gimnasio.
     * Valida los datos ingresados, crea la cuenta del miembro y asigna el plan seleccionado.
     *
     * @param nombre Nombre completo del miembro
     * @param email Correo electronico unico
     * @param password Contrasena de acceso
     * @param dni Documento Nacional de Identidad
     * @param telefono Numero de telefono de contacto
     * @param fechaNacimiento Fecha de nacimiento en formato ISO (YYYY-MM-DD)
     * @param plan Nombre del plan seleccionado (Basico o Premium)
     * @param redirectAttributes Atributos para mensajes flash
     * @return Redireccion al login si registro exitoso, o al formulario con mensaje de error
     */
    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   @RequestParam String dni,
                                   @RequestParam String telefono,
                                   @RequestParam String fechaNacimiento,
                                   @RequestParam String plan,
                                   RedirectAttributes redirectAttributes) {

        try {
            LocalDate fecha = LocalDate.parse(fechaNacimiento);

            String resultado = miembroService.registrarMiembro(nombre, email, password, dni, telefono, fecha, plan);

            if (resultado.startsWith("SUCCESS")) {
                redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("mensaje", resultado);
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/registro";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error: Formato de fecha inválido o datos incorrectos");
            redirectAttributes.addFlashAttribute("tipoMensaje", "error");
            return "redirect:/registro";
        }
    }

    /**
     * Muestra el panel de seleccion de tipo de usuario para login.
     * Permite elegir entre iniciar sesion como Administrador o Miembro.
     *
     * @return Vista del panel de seleccion de login
     */
    @GetMapping("/login")
    public String mostrarPanelLogin() {
        return "login";
    }

    /**
     * Muestra el formulario de inicio de sesion especifico para miembros.
     *
     * @return Vista del formulario de login de miembros
     */
    @GetMapping("/miembro/login")
    public String mostrarLoginMiembro() {
        return "miembro-login";
    }

    /**
     * Procesa el inicio de sesion de un miembro.
     * Valida las credenciales mediante DNI y contrasena, y crea la sesion si son correctas.
     *
     * @param dni DNI del miembro
     * @param password Contrasena del miembro
     * @param session Sesion HTTP para almacenar datos del miembro autenticado
     * @param redirectAttributes Atributos para mensajes flash en caso de error
     * @return Redireccion al dashboard del miembro si autenticacion exitosa, o al login con mensaje de error
     */
    @PostMapping("/miembro/login")
    public String procesarLoginMiembro(@RequestParam String dni,
                                       @RequestParam String password,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        Miembro miembro = miembroService.autenticarMiembro(dni, password);

        if (miembro != null) {
            session.setAttribute("miembro", miembro);
            return "redirect:/miembro/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "DNI o contraseña incorrectos");
            return "redirect:/miembro/login";
        }
    }

    /**
     * Cierra la sesion del usuario actual y limpia todos los datos de sesion.
     *
     * @param session Sesion HTTP invalidar
     * @return Redireccion al panel de seleccion de login
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /**
     * Maneja errores generales del sistema mostrando una pagina de error personalizada.
     *
     * @return Vista de la pagina de error
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}