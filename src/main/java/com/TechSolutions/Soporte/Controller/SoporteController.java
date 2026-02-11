package com.TechSolutions.Soporte.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    @GetMapping("/home")
    public String home(HttpSession session) {

        if (!"SOPORTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        return "soporte/home";
    }
}
