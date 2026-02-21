// src/main/java/com/TechSolutions/Soporte/Controller/LoginController.java
package com.TechSolutions.Soporte.Controller;

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
        return "login"; // Nombre de tu plantilla Thymeleaf para el login
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
                case "TECNICO":
                    return "redirect:/tecnico/home";
                case "CLIENTE":
                	return "redirect:/cliente/home";
                case "JEFE_SOPORTE":
                    return "redirect:/soporte/home";
                default:
                    return "redirect:/login";
            }
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

