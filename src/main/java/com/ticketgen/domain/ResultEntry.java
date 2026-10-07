package com.ticketgen.domain;

import java.time.LocalDateTime;

public record ResultEntry(Student student, int ticketNumber, LocalDateTime createdAt, boolean repeat) {
}
