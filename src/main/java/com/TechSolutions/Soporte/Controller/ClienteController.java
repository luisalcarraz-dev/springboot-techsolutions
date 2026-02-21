// src/main/java/com/TechSolutions.Soporte/Controller/ClienteController.java
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

    // Tu método dashboard, renombrado a home para mantener la consistencia de rutas
    @GetMapping("/home") // O puedes mantenerlo como "/dashboard" si prefieres esa ruta
    public String home(HttpSession session, Model model) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        // Obtener estadísticas del cliente
        Map<String, Long> estadisticas = clienteService.obtenerEstadisticas(cliente.getIdUsuario());
        model.addAttribute("estadisticas", estadisticas);
        
        // Obtener últimos tickets del cliente
        List<Incidencia> misIncidencias = clienteService.obtenerUltimosTickets(cliente.getIdUsuario(), 10); // Usamos misIncidencias para la tabla
        model.addAttribute("misIncidencias", misIncidencias); // Renombrado de 'tickets' a 'misIncidencias' para consistencia
        model.addAttribute("cliente", cliente); // Pasar el objeto cliente al modelo

        return "cliente/home"; // Asumo que tu dashboard del cliente se llama home.html o dashboard.html
    }

    // Tu método verTicket
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
            return "redirect:/cliente/home"; // Redirigir al home/dashboard del cliente
        }

        // Obtener la Orden de Trabajo si existe para mostrar la solución
        OrdenTrabajo ordenTrabajo = null;
        if (ticket.getAsignacion() != null) {
            ordenTrabajo = clienteService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        }
        model.addAttribute("ordenTrabajo", ordenTrabajo); // Pasar la OrdenTrabajo al modelo

        model.addAttribute("ticket", ticket);
        model.addAttribute("cliente", cliente); // Pasar el objeto cliente al modelo
        return "cliente/ticket-detalle"; // Asumo que tienes una vista para el detalle del ticket
    }

    // Método para mostrar el acta de conformidad (anteriormente tu /conformidad/{id})
    // He mantenido la ruta /ticket/{idIncidencia}/conformidad para consistencia con el flujo técnico
    @GetMapping("/ticket/{idIncidencia}/conformidad")
    public String mostrarActaConformidad(@PathVariable Integer idIncidencia, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String rol = (String) session.getAttribute("rol");
        Usuario cliente = (Usuario) session.getAttribute("usuario");

        if (cliente == null || !"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        Incidencia ticket = clienteService.buscarTicketCliente(idIncidencia, cliente.getIdUsuario()); // Usar buscarTicketCliente
        if (ticket == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Acceso denegado o Ticket no encontrado.");
            return "redirect:/cliente/home";
        }

        // Obtener la Orden de Trabajo si existe para mostrar la solución
        OrdenTrabajo ordenTrabajo = null;
        if (ticket.getAsignacion() != null) {
            ordenTrabajo = clienteService.obtenerOrdenTrabajoPorAsignacionId(ticket.getAsignacion().getIdAsignacion());
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("ordenTrabajo", ordenTrabajo); // Pasar la OrdenTrabajo al modelo
        model.addAttribute("cliente", cliente);

        return "cliente/acta-conformidad"; // Tu vista se llama acta-conformidad.html
    }

    // Método para procesar la acción de conformidad (POST)
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
        return "redirect:/cliente/home"; // Redirigir al dashboard del cliente
    }
}
