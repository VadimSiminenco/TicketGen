package com.ticketgen.domain;

import java.util.List;

public record Ticket(int number, List<String> questions) {
    public Ticket {
        if (number < 1 || questions.size() < 3) {
            throw new IllegalArgumentException("Invalid ticket");
        }
        questions = List.copyOf(questions);
    }

    public List<String> displayedQuestions() { return questions.subList(0, 3); }
}
