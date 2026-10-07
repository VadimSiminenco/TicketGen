package com.ticketgen.domain;

import java.util.List;

public record GenerationResult(int ticketNumber, List<String> questions, boolean repeat) {
}
