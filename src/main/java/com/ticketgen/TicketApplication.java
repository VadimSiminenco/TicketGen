package com.ticketgen;

import com.ticketgen.console.ConsoleInput;
import com.ticketgen.domain.StudentRecord;
import com.ticketgen.repository.JournalRepository;
import com.ticketgen.service.TicketGenerator;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public final class TicketApplication {
    private final ConsoleInput console;
    private final TicketGenerator ticketGenerator;
    private final JournalRepository journalRepository;
    private final Clock clock;

    public TicketApplication(
            ConsoleInput console,
            TicketGenerator ticketGenerator,
            JournalRepository journalRepository,
            Clock clock
    ) {
        this.console = Objects.requireNonNull(console);
        this.ticketGenerator = Objects.requireNonNull(ticketGenerator);
        this.journalRepository = Objects.requireNonNull(journalRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    public void run() throws IOException {
        console.println("Для выхода нажмите ESC.");

        while (true) {
            Optional<String> lastName = console.readRequiredValue("Last name:");
            if (lastName.isEmpty()) {
                finish();
                return;
            }

            Optional<String> firstName = console.readRequiredValue("First name:");
            if (firstName.isEmpty()) {
                finish();
                return;
            }

            int ticketNumber = ticketGenerator.generate();
            StudentRecord record = new StudentRecord(
                    lastName.orElseThrow(),
                    firstName.orElseThrow(),
                    ticketNumber,
                    LocalDateTime.now(clock)
            );

            try {
                journalRepository.append(record);
                console.println("Билет № " + ticketNumber);
            } catch (AccessDeniedException error) {
                showSaveError();
            } catch (FileSystemException error) {
                showSaveError();
            } catch (IOException error) {
                showSaveError();
            }
        }
    }

    private void showSaveError() {
        console.println("Не удалось сохранить данные в journal.xlsx.");
        console.println("Возможно, файл открыт в Excel.");
        console.println("Закройте journal.xlsx и повторите попытку.");
    }

    private void finish() {
        console.println("Работа программы завершена.");
    }
}
