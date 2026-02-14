package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.Repository.IncidenciaRepository;
import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    private final IncidenciaRepository incidenciaRepository;

    public SoporteController(IncidenciaRepository incidenciaRepository) {
        this.incidenciaRepository = incidenciaRepository;
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {

        // ✅ Solo Jefe de Soporte
        if (!"JEFE_SOPORTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        List<Incidencia> abiertas = incidenciaRepository
                .findByEstado_NombreOrderByIdIncidenciaDesc("ABIERTO");

        List<Incidencia> enProceso = incidenciaRepository
                .findByEstado_NombreOrderByIdIncidenciaDesc("EN_PROCESO");

        List<Incidencia> cerradas = incidenciaRepository
                .findByEstado_NombreOrderByIdIncidenciaDesc("CERRADO");

        model.addAttribute("abiertas", abiertas);
        model.addAttribute("enProceso", enProceso);
        model.addAttribute("cerradas", cerradas);

        return "soporte/home";
    }

    // Detalle de ticket (solo lectura por ahora)
    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, HttpSession session, Model model) {

        if (!"JEFE_SOPORTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        Incidencia t = incidenciaRepository.findById(id).orElse(null);
        if (t == null) return "redirect:/soporte/home";

        model.addAttribute("ticket", t);
        return "soporte/ticket-detalle";
    }
}
