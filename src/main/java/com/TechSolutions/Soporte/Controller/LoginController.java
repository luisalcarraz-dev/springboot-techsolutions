package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.LoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String validarLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        Usuario usuario = loginService.validarLogin(username, password);

        if (usuario != null) {

            session.setAttribute("usuario", usuario);
            session.setAttribute("rol", usuario.getRol().getNombre());


            switch (usuario.getRol().getNombre()) {
                case "ADMIN":
                    return "redirect:/admin/home";
                case "TECNICO":
                    return "redirect:/tecnico/home";
                case "CLIENTE":
                	return "redirect:/cliente/dashboard";
                case "JEFE_SOPORTE":
                    return "redirect:/soporte/home";
                default:
                    return "redirect:/login";
            }
        }


        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }


    @GetMapping("/registro-incidente")
    public String mostrarRegistro(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("incidencia", new Incidencia());

        return "registro-incidente";
    }

    


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    
}
