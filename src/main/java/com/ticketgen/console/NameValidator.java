package com.ticketgen.console;

import java.util.Optional;

public final class NameValidator {
    public Optional<String> normalize(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? Optional.empty() : Optional.of(normalized);
    }
}
