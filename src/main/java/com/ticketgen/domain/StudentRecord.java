package com.ticketgen.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public record StudentRecord(
        String lastName,
        String firstName,
        int ticketNumber,
        LocalDateTime createdAt
) {
    public StudentRecord {
        Objects.requireNonNull(lastName);
        Objects.requireNonNull(firstName);
        Objects.requireNonNull(createdAt);

        if (lastName.isBlank() || firstName.isBlank()) {
            throw new IllegalArgumentException("Student names must not be blank.");
        }
        if (ticketNumber < 1 || ticketNumber > 20) {
            throw new IllegalArgumentException("Ticket number must be between 1 and 20.");
        }
    }
}
