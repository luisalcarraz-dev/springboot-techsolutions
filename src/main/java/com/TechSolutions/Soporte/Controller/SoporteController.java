// src/main/java/com/TechSolutions.Soporte/Controller/SoporteController.java
package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.OrdenTrabajo; // Importar OrdenTrabajo
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.SoporteService;
import com.TechSolutions.Soporte.service.SoporteService.CargaTrabajoTecnicoDTO;
import com.TechSolutions.Soporte.service.TecnicoService; // Necesitamos TecnicoService para obtener OrdenTrabajo
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/soporte")
public class SoporteController {

    @Autowired
    private SoporteService soporteService;
    @Autowired
    private TecnicoService tecnicoService; // Inyectar TecnicoService para obtener OrdenTrabajo

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");
        Usuario jefeSoporte = (Usuario) session.getAttribute("usuario");

        if (jefeSoporte == null || !"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        List<Incidencia> todasLasIncidencias = soporteService.findAllIncidencias();

        Map<String, Long> kpis = soporteService.calcularKpis(todasLasIncidencias);
        model.addAttribute("kpis", kpis);

        List<Incidencia> incidenciasAtrasadasCriticas = soporteService.getIncidenciasAtrasadasCriticas();
        model.addAttribute("incidenciasAtrasadasCriticas", incidenciasAtrasadasCriticas);

        List<CargaTrabajoTecnicoDTO> cargaTrabajoTecnicos = soporteService.getCargaTrabajoTecnicos();
        model.addAttribute("cargaTrabajoTecnicos", cargaTrabajoTecnicos);
        model.addAttribute("jefeSoporte", jefeSoporte);

        return "soporte/home";
    }

    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        if (!"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = soporteService.buscarIncidenciaPorId(id);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket no encontrado.");
            return "redirect:/soporte/home";
        }

        model.addAttribute("ticket", ticket);
        return "soporte/ticket-detalle";
    }

    @GetMapping("/ticket/{idIncidencia}/asignar")
    public String mostrarFormularioAsignacion(@PathVariable Integer idIncidencia, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario jefeSoporte = (Usuario) session.getAttribute("usuario");

        if (jefeSoporte == null || !"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = soporteService.buscarIncidenciaPorId(idIncidencia);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Incidencia no encontrada para asignar.");
            return "redirect:/soporte/home";
        }

        if (ticket.getAsignacion() != null && ticket.getAsignacion().getActiva()) {
            redirectAttributes.addFlashAttribute("infoMessage", "La incidencia ya está asignada. Utilice la función 'Revisar' para reasignar.");
            return "redirect:/soporte/home";
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("tiposIncidencia", soporteService.findAllTiposIncidencia());
        model.addAttribute("prioridades", soporteService.findAllPrioridades());
        model.addAttribute("tecnicosDisponibles", soporteService.findAllTecnicos());
        model.addAttribute("jefeSoporte", jefeSoporte);

        return "soporte/asignar-incidencia";
    }

    @PostMapping("/ticket/{idIncidencia}/asignar")
    public String asignarIncidencia(@PathVariable Integer idIncidencia,
                                   @RequestParam Integer tipoId,
                                   @RequestParam Integer prioridadId,
                                   @RequestParam Integer tiempoObjetivoHoras,
                                   @RequestParam Integer tecnicoId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario jefeSoporte = (Usuario) session.getAttribute("usuario");

        if (jefeSoporte == null || !"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        try {
            soporteService.asignarYClasificarIncidencia(idIncidencia, tipoId, prioridadId, tiempoObjetivoHoras, tecnicoId);
            redirectAttributes.addFlashAttribute("successMessage", "Incidencia asignada y clasificada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar incidencia: " + e.getMessage());
            return "redirect:/soporte/ticket/" + idIncidencia + "/asignar";
        }
        return "redirect:/soporte/home";
    }

    @GetMapping("/ticket/{idIncidencia}/revisar")
    public String mostrarFormularioRevision(@PathVariable Integer idIncidencia, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario jefeSoporte = (Usuario) session.getAttribute("usuario");

        if (jefeSoporte == null || !"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = soporteService.buscarIncidenciaPorId(idIncidencia);
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Incidencia no encontrada para revisar.");
            return "redirect:/soporte/home";
        }

        // --- CAMBIO AQUÍ: Obtener la OrdenTrabajo si existe una asignación ---
        OrdenTrabajo ordenTrabajo = null;
        if (ticket.getAsignacion() != null) {
            ordenTrabajo = tecnicoService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        }
        // -------------------------------------------------------------------

        model.addAttribute("ticket", ticket);
        model.addAttribute("ordenTrabajo", ordenTrabajo); // Pasar la OrdenTrabajo al modelo
        model.addAttribute("tecnicosDisponibles", soporteService.findAllTecnicos());
        model.addAttribute("jefeSoporte", jefeSoporte);

        return "soporte/revisar-ticket";
    }

    @PostMapping("/ticket/{idIncidencia}/revisar")
    public String procesarAccionRevision(@PathVariable Integer idIncidencia,
                                         @RequestParam String accion,
                                         @RequestParam(required = false) Integer nuevoTecnicoId,
                                         @RequestParam(required = false) String observacionesJefe,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario jefeSoporte = (Usuario) session.getAttribute("usuario");

        if (jefeSoporte == null || !"JEFE_SOPORTE".equals(rol)) {
            return "redirect:/login";
        }

        try {
            switch (accion) {
                case "reasignar":
                    if (nuevoTecnicoId == null) {
                        throw new RuntimeException("Debe seleccionar un técnico para reasignar.");
                    }
                    soporteService.reasignarTecnico(idIncidencia, nuevoTecnicoId, observacionesJefe);
                    redirectAttributes.addFlashAttribute("successMessage", "Incidencia reasignada exitosamente.");
                    break;
                case "solicitarApoyo":
                    soporteService.solicitarApoyo(idIncidencia);
                    redirectAttributes.addFlashAttribute("successMessage", "Solicitud de apoyo enviada para la incidencia.");
                    break;
                case "guardar":
                    soporteService.guardarObservacionesJefe(idIncidencia, observacionesJefe);
                    redirectAttributes.addFlashAttribute("successMessage", "Observaciones guardadas exitosamente.");
                    break;
                default:
                    throw new RuntimeException("Acción no reconocida.");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la acción: " + e.getMessage());
        }
        return "redirect:/soporte/ticket/" + idIncidencia + "/revisar";
    }
}
