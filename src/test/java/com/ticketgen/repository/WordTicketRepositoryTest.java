package com.ticketgen.repository;

import com.ticketgen.TestFiles;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class WordTicketRepositoryTest {
    @TempDir Path directory;

    @Test
    void skipsShortTicketAndKeepsValidTicketsAfterIt() throws Exception {
        TestFiles.tickets(directory);
        var tickets = new WordTicketRepository(directory.resolve("tickets.docx")).loadTickets();
        assertEquals(2, tickets.size());
        assertEquals(7, tickets.get(0).number());
        assertEquals(15, tickets.get(1).number());
        assertEquals(3, tickets.get(0).displayedQuestions().size());
        assertEquals(4, tickets.get(1).questions().size());
    }

    @Test
    void malformedHeaderDoesNotConsumeFollowingValidTicket() throws Exception {
        try (XWPFDocument doc = new XWPFDocument()) {
            for (String text : new String[]{"Билет X", "1. Bad", "Билет 31", "1. A", "2. B", "3. C"}) {
                doc.createParagraph().createRun().setText(text);
            }
            try (OutputStream out = Files.newOutputStream(directory.resolve("tickets.docx"))) {
                doc.write(out);
            }
        }
        assertEquals(31, new WordTicketRepository(directory.resolve("tickets.docx"))
                .loadTickets().get(0).number());
    }
}
