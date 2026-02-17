package com.TechSolutions.Soporte.Controller;

import com.TechSolutions.Soporte.model.Incidencia;
import com.TechSolutions.Soporte.model.Usuario;
import com.TechSolutions.Soporte.service.ClienteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Verificar que sea CLIENTE
        if (!"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        Usuario cliente = (Usuario) session.getAttribute("usuario");
        
        // Obtener estadísticas del cliente
        Map<String, Long> estadisticas = clienteService.obtenerEstadisticas(cliente.getIdUsuario());
        model.addAttribute("estadisticas", estadisticas);
        
        // Obtener últimos tickets del cliente
        List<Incidencia> tickets = clienteService.obtenerUltimosTickets(cliente.getIdUsuario(), 10);
        model.addAttribute("tickets", tickets);
        
        return "cliente/dashboard";
    }

    @GetMapping("/ticket/{id}")
    public String verTicket(@PathVariable Integer id, HttpSession session, Model model) {
        if (!"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        Usuario cliente = (Usuario) session.getAttribute("usuario");
        Incidencia ticket = clienteService.buscarTicketCliente(id, cliente.getIdUsuario());
        
        if (ticket == null) {
            return "redirect:/cliente/dashboard";
        }

        model.addAttribute("ticket", ticket);
        return "cliente/ticket-detalle";
    }

    @GetMapping("/conformidad/{id}")
    public String mostrarConformidad(@PathVariable Integer id, HttpSession session, Model model) {
        if (!"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        Usuario cliente = (Usuario) session.getAttribute("usuario");
        Incidencia ticket = clienteService.buscarTicketCliente(id, cliente.getIdUsuario());
        
        if (ticket == null || !"CERRADO".equals(ticket.getEstado().getNombre())) {
            return "redirect:/cliente/dashboard";
        }

        model.addAttribute("ticket", ticket);
        return "cliente/conformidad";
    }

    @PostMapping("/conformidad/{id}")
    public String guardarConformidad(@PathVariable Integer id,
                                     @RequestParam Boolean conforme,
                                     @RequestParam(required = false) String comentario,
                                     HttpSession session) {
        if (!"CLIENTE".equals(session.getAttribute("rol"))) {
            return "redirect:/login";
        }

        Usuario cliente = (Usuario) session.getAttribute("usuario");
        clienteService.guardarConformidad(id, cliente.getIdUsuario(), conforme, comentario);
        
        return "redirect:/cliente/dashboard";
    }
}