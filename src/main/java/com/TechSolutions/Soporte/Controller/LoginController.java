package com.TechSolutions.Soporte.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final String HASH_ADMIN =
        "$2a$10$4Twm7RuW0AlTV4Gp1Ycu5ePs1Mb6alG2gtnUw4tNxG8rIIMkdZhEC";

    private final String HASH_SOPORTE =
        "$2a$10$xRLXZocQYapY1wI9H/EyN.CxOQnSSf68alymeCOMWmV7KIDPBE58i";

    private final String HASH_USUARIO =
        "$2a$10$bYyZ4YaAZF79NO3o3DYpeuwZkm3dq0G8o95J0a9OPsh.8v4hMu/um";

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

        if (username.equals("admin") && encoder.matches(password, HASH_ADMIN)) {
            session.setAttribute("usuario", "admin");
            session.setAttribute("rol", "ADMIN");
            return "redirect:/admin/home";
        }

        if (username.equals("soporte") && encoder.matches(password, HASH_SOPORTE)) {
            session.setAttribute("usuario", "soporte");
            session.setAttribute("rol", "SOPORTE");
            return "redirect:/soporte/home";
        }

        if (username.equals("usuario") && encoder.matches(password, HASH_USUARIO)) {
            session.setAttribute("usuario", "usuario");
            session.setAttribute("rol", "CLIENTE");
            return "redirect:/usuario/home";
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }
}
