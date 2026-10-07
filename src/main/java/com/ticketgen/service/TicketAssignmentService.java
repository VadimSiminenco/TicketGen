package com.ticketgen.service;

import com.ticketgen.domain.GenerationResult;
import com.ticketgen.domain.ResultEntry;
import com.ticketgen.domain.Student;
import com.ticketgen.domain.Ticket;
import com.ticketgen.exception.SourceDataException;
import com.ticketgen.repository.ResultRepository;
import com.ticketgen.repository.StudentRepository;
import com.ticketgen.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.random.RandomGenerator;

@Service
public class TicketAssignmentService {
    private final StudentRepository students;
    private final TicketRepository tickets;
    private final ResultRepository results;
    private final RandomGenerator random;
    private final Clock clock;

    @Autowired
    public TicketAssignmentService(StudentRepository students, TicketRepository tickets, ResultRepository results) {
        this(students, tickets, results, RandomGenerator.getDefault(), Clock.systemDefaultZone());
    }

    public TicketAssignmentService(StudentRepository students, TicketRepository tickets, ResultRepository results,
                                   RandomGenerator random, Clock clock) {
        this.students = students;
        this.tickets = tickets;
        this.results = results;
        this.random = random;
        this.clock = clock;
    }

    public Map<String, List<Student>> groups() { return students.loadGroups(); }
    public List<Ticket> validTickets() { return tickets.loadTickets(); }

    public synchronized GenerationResult generate(String group, String studentKey) {
        List<Student> groupStudents = groups().get(group);
        if (groupStudents == null) throw new IllegalArgumentException("Неизвестная группа.");
        Student student = groupStudents.stream()
                .filter(candidate -> candidate.key().equals(studentKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден в выбранной группе."));
        List<Ticket> valid = validTickets();
        if (valid.isEmpty()) throw new SourceDataException("В tickets.docx нет подходящих билетов.");

        OptionalInt first = results.firstTicketFor(student);
        Ticket ticket;
        if (first.isPresent()) {
            int originalNumber = first.getAsInt();
            ticket = valid.stream().filter(candidate -> candidate.number() == originalNumber)
                    .findFirst()
                    .orElseThrow(() -> new SourceDataException(
                            "Первый билет студента отсутствует среди действительных билетов."));
        } else {
            ticket = valid.get(random.nextInt(valid.size()));
        }

        results.append(new ResultEntry(student, ticket.number(), LocalDateTime.now(clock), first.isPresent()));
        return new GenerationResult(ticket.number(), ticket.displayedQuestions(), first.isPresent());
    }
}
