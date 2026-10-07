package com.ticketgen.repository;

import com.ticketgen.domain.Ticket;
import java.util.List;

public interface TicketRepository {
    List<Ticket> loadTickets();
}
