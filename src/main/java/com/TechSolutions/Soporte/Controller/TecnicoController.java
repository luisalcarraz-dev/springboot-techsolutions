// src/main/java/com/TechSolutions.Soporte/Controller/TecnicoController.java
package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.OrdenTrabajo;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.model.EstadoIncidencia;
import com.TechSolutions.Soporte.service.TecnicoService;
import com.TechSolutions.Soporte.service.EstadoIncidenciaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tecnico")
public class TecnicoController {

    @Autowired
    private TecnicoService tecnicoService;

    @Autowired
    private EstadoIncidenciaService estadoIncidenciaService;

    @GetMapping("/home")
    public String tecnicoHome(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");
        Usuario tecnico = (Usuario) session.getAttribute("usuario");

        if (tecnico == null || !"TECNICO".equals(rol)) {
            return "redirect:/login";
        }

        List<Incidencia> incidenciasAsignadas = tecnicoService.getIncidenciasAsignadas(tecnico.getIdUsuario());
        model.addAttribute("incidencias", incidenciasAsignadas);
        model.addAttribute("tecnico", tecnico);

        Map<String, Long> estadisticas = tecnicoService.obtenerEstadisticasDashboard(incidenciasAsignadas);
        model.addAttribute("estadisticas", estadisticas);

        return "tecnico/home";
    }

    @GetMapping("/ticket/{idIncidencia}")
    public String verOrdenTrabajo(@PathVariable Integer idIncidencia, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario tecnico = (Usuario) session.getAttribute("usuario");

        if (tecnico == null || !"TECNICO".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = tecnicoService.buscarIncidenciaPorId(idIncidencia);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Incidencia no encontrada.");
            return "redirect:/tecnico/home";
        }

        if (ticket.getAsignacion() == null || !ticket.getAsignacion().getTecnico().getIdUsuario().equals(tecnico.getIdUsuario())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Esta incidencia no está asignada a usted.");
            return "redirect:/tecnico/home";
        }

        OrdenTrabajo ordenTrabajo = tecnicoService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        if (ordenTrabajo == null) {
            ordenTrabajo = new OrdenTrabajo();
        }

        List<EstadoIncidencia> estadosDisponibles = estadoIncidenciaService.findAllEstados();

        model.addAttribute("ticket", ticket);
        model.addAttribute("ordenTrabajo", ordenTrabajo);
        model.addAttribute("estadosDisponibles", estadosDisponibles);
        model.addAttribute("tecnico", tecnico);

        return "tecnico/orden-trabajo";
    }

    @PostMapping("/ticket/{idIncidencia}/guardar-avance")
    public String guardarAvanceOrdenTrabajo(@PathVariable Integer idIncidencia,
                                            @ModelAttribute OrdenTrabajo ordenTrabajo,
                                            @RequestParam Integer nuevoEstadoId,
                                            @RequestParam(required = false) boolean solicitarCierre,
                                            HttpSession session,
                                            RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario tecnico = (Usuario) session.getAttribute("usuario");

        if (tecnico == null || !"TECNICO".equals(rol)) {
            return "redirect:/login";
        }

        try {
            tecnicoService.guardarOrdenTrabajoYActualizarIncidencia(idIncidencia, tecnico.getIdUsuario(), ordenTrabajo, nuevoEstadoId, solicitarCierre);
            redirectAttributes.addFlashAttribute("successMessage", "Avance de la orden de trabajo guardado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el avance: " + e.getMessage());
        }

        // Redirigir al dashboard del técnico
        return "redirect:/tecnico/home";
    }
}
