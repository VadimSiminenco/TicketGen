package com.ticketgen.console;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameValidatorTest {
    private final NameValidator validator = new NameValidator();

    @Test
    void trimsLeadingAndTrailingSpaces() {
        assertEquals("Ivanov", validator.normalize("   Ivanov   ").orElseThrow());
    }

    @Test
    void rejectsEmptyAndWhitespaceOnlyValues() {
        assertTrue(validator.normalize("").isEmpty());
        assertTrue(validator.normalize("   ").isEmpty());
    }
}
