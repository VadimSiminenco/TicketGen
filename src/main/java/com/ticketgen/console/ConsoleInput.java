package com.ticketgen.console;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

public final class ConsoleInput implements AutoCloseable {
    private static final int ESCAPE = 27;
    private static final int BACKSPACE = 8;
    private static final int DELETE = 127;

    private final Reader reader;
    private final PrintWriter writer;
    private final NameValidator validator;
    private final Closeable closeAction;
    private boolean skipNextLineFeed;

    ConsoleInput(Reader reader, PrintWriter writer) {
        this(reader, writer, () -> {
        });
    }

    private ConsoleInput(Reader reader, PrintWriter writer, Closeable closeAction) {
        this.reader = Objects.requireNonNull(reader);
        this.writer = Objects.requireNonNull(writer);
        this.closeAction = Objects.requireNonNull(closeAction);
        this.validator = new NameValidator();
    }

    public static ConsoleInput openSystemConsole() throws IOException {
        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .build();
        Attributes originalAttributes = terminal.enterRawMode();

        Closeable closeAction = () -> {
            try {
                terminal.setAttributes(originalAttributes);
            } finally {
                terminal.close();
            }
        };

        return new ConsoleInput(terminal.reader(), terminal.writer(), closeAction);
    }

    public Optional<String> readRequiredValue(String prompt) throws IOException {
        while (true) {
            writer.print(prompt);
            writer.print(' ');
            writer.flush();

            Optional<String> value = readLineOrEscape();
            if (value.isEmpty()) {
                return Optional.empty();
            }

            Optional<String> normalized = validator.normalize(value.orElseThrow());
            if (normalized.isPresent()) {
                return normalized;
            }
        }
    }

    public void println(String message) {
        writer.println(message);
        writer.flush();
    }

    private Optional<String> readLineOrEscape() throws IOException {
        StringBuilder value = new StringBuilder();

        while (true) {
            int character = reader.read();

            if (character == -1 || character == ESCAPE) {
                writer.println();
                writer.flush();
                return Optional.empty();
            }

            if (skipNextLineFeed && character == '\n') {
                skipNextLineFeed = false;
                continue;
            }
            skipNextLineFeed = false;

            if (character == '\r' || character == '\n') {
                skipNextLineFeed = character == '\r';
                writer.println();
                writer.flush();
                return Optional.of(value.toString());
            }

            if (character == BACKSPACE || character == DELETE) {
                eraseLastCharacter(value);
                continue;
            }

            if (!Character.isISOControl(character)) {
                value.append((char) character);
                writer.print((char) character);
                writer.flush();
            }
        }
    }

    private void eraseLastCharacter(StringBuilder value) {
        if (value.isEmpty()) {
            return;
        }

        value.deleteCharAt(value.length() - 1);
        writer.print("\b \b");
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        closeAction.close();
    }
}
