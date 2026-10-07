package com.ticketgen;

import com.ticketgen.console.ConsoleInput;
import com.ticketgen.repository.ExcelJournalRepository;
import com.ticketgen.repository.JournalRepository;
import com.ticketgen.service.TicketGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        JournalRepository repository = new ExcelJournalRepository(Path.of("journal.xlsx"));

        try (ConsoleInput console = ConsoleInput.openSystemConsole()) {
            TicketApplication application = new TicketApplication(
                    console,
                    new TicketGenerator(),
                    repository,
                    Clock.systemDefaultZone()
            );
            application.run();
        } catch (IOException error) {
            System.err.println("Не удалось продолжить работу с консолью.");
        }
    }
}
