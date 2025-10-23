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

@Controller
public class HomeController {

    @Autowired
    private PlanService planService;

    @Autowired
    private MiembroService miembroService;

    // Página de inicio
    @GetMapping("/")
    public String mostrarHome(Model model) {
        // Obtener los planes activos desde la base de datos
        List<Plan> planes = planService.obtenerPlanesActivos();

        // Buscar específicamente el plan Básico y Premium
        Plan planBasico = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Basico"))
                .findFirst()
                .orElse(null);

        Plan planPremium = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Premium"))
                .findFirst()
                .orElse(null);

        // Pasar los planes al modelo
        model.addAttribute("planBasico", planBasico);
        model.addAttribute("planPremium", planPremium);

        return "home";
    }

    // Página "Nosotros"
    @GetMapping("/nosotros")
    public String mostrarNosotros() {
        return "nosotros";
    }

    // Página FAQ
    @GetMapping("/faq")
    public String mostrarFaq() {
        return "faq";
    }

    // Mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        // Obtener los planes activos desde la base de datos
        List<Plan> planes = planService.obtenerPlanesActivos();

        // Buscar específicamente el plan Básico y Premium
        Plan planBasico = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Basico"))
                .findFirst()
                .orElse(null);

        Plan planPremium = planes.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase("Premium"))
                .findFirst()
                .orElse(null);

        // Pasar los planes al modelo
        model.addAttribute("planBasico", planBasico);
        model.addAttribute("planPremium", planPremium);

        return "registro";
    }

    // Procesar registro de nuevo miembro
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


    // Mostrar panel de selección de login (Administrador/Miembro)
    @GetMapping("/login")
    public String mostrarPanelLogin() {
        return "login";
    }

    // Mostrar login específico para miembros
    @GetMapping("/miembro/login")
    public String mostrarLoginMiembro() {
        return "miembro-login";
    }

    // Procesar login de miembros
    @PostMapping("/miembro/login")
    public String procesarLoginMiembro(@RequestParam String dni,
                                       @RequestParam String password,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {

        Miembro miembro = miembroService.autenticarMiembro(dni, password);

        if (miembro != null) {
            // Guardar miembro en sesión
            session.setAttribute("miembro", miembro);
            return "redirect:/miembro/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "DNI o contraseña incorrectos");
            return "redirect:/miembro/login";
        }
    }

    // Cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // Manejo de errores
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}