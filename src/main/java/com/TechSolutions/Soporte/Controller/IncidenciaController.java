package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.IncidenciaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    // Mostrar formulario
    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Si no hay login, manda a login (recomendado)
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("incidencia", new Incidencia());

        return "registro-incidente";
    }

    // Registrar incidencia y redirigir a la vista del ticket generado
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Incidencia incidencia, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        incidencia.setCliente(usuario);

        // Guardar y recuperar el ticket con su ID
        Incidencia guardada = incidenciaService.registrarIncidencia(incidencia);

        // Mostrar ticket generado
        return "redirect:/incidencias/ticket/" + guardada.getIdIncidencia();
    }

    // Mostrar ticket por id
    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, Model model, HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Incidencia ticket = incidenciaService.buscarPorId(id);
        if (ticket == null) {
            return "redirect:/incidencias/nuevo";
        }

        model.addAttribute("ticket", ticket);
        return "ticket-generado";
    }
}
