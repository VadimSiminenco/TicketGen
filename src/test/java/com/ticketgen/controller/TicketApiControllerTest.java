package com.ticketgen.controller;

import com.ticketgen.domain.Student;
import com.ticketgen.domain.Ticket;
import com.ticketgen.exception.ResultFileLockedException;
import com.ticketgen.repository.ResultRepository;
import com.ticketgen.repository.StudentRepository;
import com.ticketgen.repository.TicketRepository;
import com.ticketgen.service.TicketAssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TicketApiControllerTest {
    @Test
    void lockedFileReturnsRetryStateWithoutSuccess() {
        Student student = new Student("TI-221", "Ivanov", "Ivan");
        StudentRepository students = () -> Map.of(student.group(), List.of(student));
        TicketRepository tickets = () -> List.of(new Ticket(7, List.of("A", "B", "C")));
        ResultRepository locked = new ResultRepository() {
            @Override public OptionalInt firstTicketFor(Student value) { return OptionalInt.empty(); }
            @Override public void append(com.ticketgen.domain.ResultEntry value) {
                throw new ResultFileLockedException(new IOException("locked"));
            }
        };
        var service = new TicketAssignmentService(students, tickets, locked, new Random(1), Clock.systemUTC());
        var controller = new TicketApiController(service);
        var problem = assertThrows(ResultFileLockedException.class,
                () -> controller.generate(new TicketApiController.GenerateRequest(student.group(), student.key())));
        var response = controller.locked(problem);
        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertEquals("locked", response.getBody().code());
    }
}
