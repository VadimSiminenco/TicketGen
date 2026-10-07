package com.ticketgen.console;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleInputTest {
    @Test
    void repeatsPromptAfterBlankInputAndReturnsTrimmedValue() throws Exception {
        StringWriter output = new StringWriter();

        try (ConsoleInput input = new ConsoleInput(
                new StringReader("\r\n   \r\n  Ivanov  \r\n"),
                new PrintWriter(output)
        )) {
            assertEquals("Ivanov", input.readRequiredValue("Last name:").orElseThrow());
        }

        assertEquals(3, countOccurrences(output.toString(), "Last name:"));
    }

    @Test
    void escapeReturnsAnEmptyResultWithoutEnter() throws Exception {
        try (ConsoleInput input = new ConsoleInput(
                new StringReader("\u001Bignored"),
                new PrintWriter(new StringWriter())
        )) {
            assertTrue(input.readRequiredValue("Last name:").isEmpty());
        }
    }

    @Test
    void backspaceRemovesThePreviousCharacter() throws Exception {
        try (ConsoleInput input = new ConsoleInput(
                new StringReader("IvaX\bnov\r"),
                new PrintWriter(new StringWriter())
        )) {
            assertEquals("Ivanov", input.readRequiredValue("Last name:").orElseThrow());
        }
    }

    private int countOccurrences(String text, String value) {
        return (text.length() - text.replace(value, "").length()) / value.length();
    }
}
