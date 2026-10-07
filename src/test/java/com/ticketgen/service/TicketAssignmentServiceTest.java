package com.ticketgen.service;

import com.ticketgen.TestFiles;
import com.ticketgen.domain.Student;
import com.ticketgen.repository.ExcelResultRepository;
import com.ticketgen.repository.ExcelStudentRepository;
import com.ticketgen.repository.WordTicketRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TicketAssignmentServiceTest {
    @TempDir Path directory;

    @Test
    void repeatUsesFirstTicketAndAppendsHistory() throws Exception {
        TestFiles.students(directory);
        TestFiles.tickets(directory);
        var service = service();
        Student student = new Student("TI-221", "Ivanov", "Ivan");
        var first = service.generate(student.group(), student.key());
        var repeat = service().generate(student.group(), student.key());
        assertFalse(first.repeat());
        assertTrue(repeat.repeat());
        assertEquals(first.ticketNumber(), repeat.ticketNumber());
        assertEquals(3, repeat.questions().size());
        Student other = new Student("TI-222", "Petrov", "Petr");
        var otherResult = service.generate(other.group(), other.key());
        assertFalse(otherResult.repeat());
        assertTrue(otherResult.ticketNumber() == 7 || otherResult.ticketNumber() == 15);
        try (var input = Files.newInputStream(directory.resolve("results.xlsx"));
             XSSFWorkbook book = new XSSFWorkbook(input)) {
            assertEquals(3, book.getSheetAt(0).getLastRowNum());
        }
        assertThrows(IllegalArgumentException.class, () -> service.generate("TI-221", "fake"));
    }

    private TicketAssignmentService service() {
        return new TicketAssignmentService(
                new ExcelStudentRepository(directory.resolve("students.xlsx")),
                new WordTicketRepository(directory.resolve("tickets.docx")),
                new ExcelResultRepository(directory.resolve("results.xlsx")),
                new Random(4),
                Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC)
        );
    }
}
