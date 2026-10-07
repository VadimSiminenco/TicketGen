package com.ticketgen.repository;

import com.ticketgen.config.DataPaths;
import com.ticketgen.domain.Ticket;
import com.ticketgen.exception.SourceDataException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class WordTicketRepository implements TicketRepository {
    private static final Logger log = LoggerFactory.getLogger(WordTicketRepository.class);
    private static final Pattern HEADER = Pattern.compile("(?iu)^Билет\\s+(\\d+)\\s*$");
    private static final Pattern QUESTION = Pattern.compile("^\\d+\\.\\s+(.+)$");
    private final Path path;

    @Autowired
    public WordTicketRepository(DataPaths paths) { this.path = paths.tickets(); }
    public WordTicketRepository(Path path) { this.path = path; }

    @Override
    public List<Ticket> loadTickets() {
        if (!Files.isRegularFile(path)) {
            log.warn("Ticket source is missing: {}", path);
            throw new SourceDataException("Не найден файл tickets.docx. Добавьте его в папку приложения.");
        }
        List<Ticket> tickets = new ArrayList<>();
        Set<Integer> seen = new HashSet<>();
        Integer number = null;
        List<String> questions = new ArrayList<>();
        try (InputStream input = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(input)) {
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String line = paragraph.getText().trim();
                if (line.isEmpty()) continue;
                Matcher header = HEADER.matcher(line);
                if (header.matches()) {
                    addTicket(tickets, seen, number, questions);
                    questions = new ArrayList<>();
                    try {
                        number = Integer.parseInt(header.group(1));
                    } catch (NumberFormatException error) {
                        log.warn("Invalid ticket number: {}", line);
                        number = null;
                    }
                } else if (line.toLowerCase().startsWith("билет")) {
                    addTicket(tickets, seen, number, questions);
                    log.warn("Malformed ticket header: {}", line);
                    number = null;
                    questions = new ArrayList<>();
                } else {
                    Matcher question = QUESTION.matcher(line);
                    if (number != null && question.matches() && !question.group(1).trim().isEmpty()) {
                        questions.add(question.group(1).trim());
                    } else {
                        log.warn("Skipping malformed ticket content: {}", line);
                    }
                }
            }
            addTicket(tickets, seen, number, questions);
            log.info("Loaded {} valid tickets", tickets.size());
            return List.copyOf(tickets);
        } catch (IOException | RuntimeException error) {
            log.error("Cannot read tickets.docx", error);
            throw new SourceDataException("Не удалось прочитать tickets.docx.", error);
        }
    }

    private void addTicket(List<Ticket> tickets, Set<Integer> seen, Integer number, List<String> questions) {
        if (number == null) return;
        if (number < 1 || questions.size() < 3 || !seen.add(number)) {
            log.warn("Skipping invalid or duplicate ticket {} ({} questions)", number, questions.size());
            return;
        }
        tickets.add(new Ticket(number, questions));
    }
}
