package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.OrdenTrabajo;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map; // Necesario para Map<String, Long>

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        Map<String, Long> estadisticas = clienteService.obtenerEstadisticas(cliente.getIdUsuario());
        model.addAttribute("estadisticas", estadisticas);

        List<Incidencia> misIncidencias = clienteService.obtenerUltimosTickets(cliente.getIdUsuario(), 10);
        model.addAttribute("misIncidencias", misIncidencias);
        model.addAttribute("cliente", cliente); 
        return "cliente/home";
    }

    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = clienteService.buscarTicketCliente(id, cliente.getIdUsuario());
        
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket no encontrado o no pertenece a este cliente.");
            return "redirect:/cliente/home"; 
        }

        OrdenTrabajo ordenTrabajo = null;
        if (ticket.getAsignacion() != null) {
            ordenTrabajo = clienteService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        }
        model.addAttribute("ordenTrabajo", ordenTrabajo);

        model.addAttribute("ticket", ticket);
        model.addAttribute("cliente", cliente);
        return "cliente/ticket-detalle"; 
    }

    @GetMapping("/ticket/{idIncidencia}/conformidad")
    public String mostrarActaConformidad(@PathVariable Integer idIncidencia, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = clienteService.buscarTicketCliente(idIncidencia, cliente.getIdUsuario()); 
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Acceso denegado o Ticket no encontrado.");
            return "redirect:/cliente/home";
        }

        OrdenTrabajo ordenTrabajo = null;
        if (ticket.getAsignacion() != null) {
            ordenTrabajo = clienteService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("ordenTrabajo", ordenTrabajo); 
        model.addAttribute("cliente", cliente);

        return "cliente/acta-conformidad";
    }


    @PostMapping("/ticket/{idIncidencia}/conformidad")
    public String procesarConformidad(@PathVariable Integer idIncidencia,
                                      @RequestParam String accion, // 'conforme' o 'noConforme'
                                      @RequestParam(required = false) String comentarioCliente,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        try {
            clienteService.procesarConformidadCliente(idIncidencia, cliente.getIdUsuario(), accion, comentarioCliente);
            redirectAttributes.addFlashAttribute("successMessage", "Conformidad registrada exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar conformidad: " + e.getMessage());
        }
        return "redirect:/cliente/home"; 
    }
}
