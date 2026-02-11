package com.TechSolutions.Soporte.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.IncidenciaService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;

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

        // Temporal si no hay login aún
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setIdUsuario(1);
            usuario.setNombres("Juan");
            usuario.setApellidos("Pérez");
            usuario.setCorreo("juan.perez@techsolutions.com");
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("incidencia", new Incidencia());

        return "registro-incidente";
    }

    // Registrar incidencia
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute Incidencia incidencia,
                            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setIdUsuario(1); // cliente existente
        }

        incidencia.setCliente(usuario);

        incidenciaService.registrarIncidencia(incidencia);

        return "redirect:/incidencias/confirmacion";
    }

    // Confirmación
    @GetMapping("/confirmacion")
    public String confirmacion() {
        return "confirmacion-ticket";
    }
}