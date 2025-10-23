package com.example.ProyectoGym.Controllers;

import com.example.ProyectoGym.Model.Miembro;
import com.example.ProyectoGym.Services.MiembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class MiembroController {

    @Autowired
    private MiembroService miembroService;

    // Página principal (Home)
    @GetMapping("/")
    public String home() {
        return "home"; // Retorna home.html
    }

    // Mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro"; // Retorna registro.html
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
            // Convertir fecha de String a LocalDate
            LocalDate fecha = LocalDate.parse(fechaNacimiento);

            // Llamar al service para registrar
            String resultado = miembroService.registrarMiembro(nombre, email, password, dni, telefono, fecha, plan);

            if (resultado.startsWith("SUCCESS")) {
                redirectAttributes.addFlashAttribute("mensaje", "¡Registro exitoso! Ya puedes iniciar sesión.");
                redirectAttributes.addFlashAttribute("tipoMensaje", "success");
                return "redirect:/login"; // Redirigir al panel de selección
            } else {
                redirectAttributes.addFlashAttribute("mensaje", resultado);
                redirectAttributes.addFlashAttribute("tipoMensaje", "error");
                return "redirect:/registro"; // Volver al formulario
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
        return "login"; // Retorna login.html (panel de selección)
    }

    // Mostrar login específico para miembros
    @GetMapping("/miembro/login")
    public String mostrarLoginMiembro() {
        return "miembro-login"; // Retorna miembro-login.html
    }

    // Mostrar login para administrador (futuro)
    @GetMapping("/admin/login")
    public String mostrarLoginAdmin() {
        return "admin-login"; // Para implementar después
    }

    // Procesar login de miembros - CORREGIDO
    @PostMapping("/miembro/login")
    public String procesarLoginMiembro(@RequestParam String dni,
                                       @RequestParam String password,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        // Autenticar miembro
        Miembro miembro = miembroService.autenticarMiembro(dni, password);

        if (miembro != null) {
            // Login exitoso - agregar miembro al modelo
            model.addAttribute("miembro", miembro);
            return "bienvenido"; // Página de bienvenida
        } else {
            // Login fallido - redirigir de vuelta al login de miembros
            redirectAttributes.addFlashAttribute("mensaje", "DNI o contraseña incorrectos");
            return "redirect:/miembro/login";
        }
    }

    // Procesar login general (mantener por compatibilidad)
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String dni,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        // Este metodo redirige al login específico de miembros
        // porque el formulario de miembro-login.html envía a /login
        return procesarLoginMiembro(dni, password, redirectAttributes, model);
    }

    // Página de bienvenida después del login exitoso
    @GetMapping("/bienvenido")
    public String bienvenido(Model model) {
        // Si alguien accede directamente sin login, redirigir
        if (!model.containsAttribute("miembro")) {
            return "redirect:/login";
        }
        return "bienvenido"; // Retorna bienvenido.html
    }

    // API para validar DNI (AJAX) - opcional pero útil
    @GetMapping("/api/validar-dni")
    @ResponseBody
    public boolean validarDni(@RequestParam String dni) {
        return !miembroService.existeDni(dni); // Retorna true si NO existe (disponible)
    }

    // API para validar email (AJAX) - opcional pero útil
    @GetMapping("/api/validar-email")
    @ResponseBody
    public boolean validarEmail(@RequestParam String email) {
        return !miembroService.existeEmail(email); // Retorna true si NO existe (disponible)
    }

    // Manejo de errores 404
    @GetMapping("/error")
    public String error() {
        return "error"; // Página de error personalizada
    }
}
