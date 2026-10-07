package com.ticketgen.controller;

import com.ticketgen.exception.SourceDataException;
import com.ticketgen.service.TicketAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);
    private final TicketAssignmentService service;

    public HomeController(TicketAssignmentService service) { this.service = service; }

    @GetMapping("/")
    public String home(Model model) {
        List<String> groups = List.of();
        String error = null;
        try {
            groups = List.copyOf(service.groups().keySet());
        } catch (SourceDataException problem) {
            error = problem.getMessage();
        }
        try {
            if (service.validTickets().isEmpty()) error = "В tickets.docx нет подходящих билетов.";
        } catch (SourceDataException problem) {
            error = error == null ? problem.getMessage() : error + " " + problem.getMessage();
        }
        if (error != null) log.warn("Generation unavailable: {}", error);
        model.addAttribute("groups", groups);
        model.addAttribute("sourceError", error);
        return "index";
    }
}
