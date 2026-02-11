package com.TechSolutions.Soporte.Controller;


import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @GetMapping("/home")
    public String home(HttpSession session) {

        if (!"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        return "cliente/home";
    }
}
