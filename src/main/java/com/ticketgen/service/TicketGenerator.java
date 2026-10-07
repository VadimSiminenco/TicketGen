package com.ticketgen.service;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public final class TicketGenerator {
    private static final int MIN_TICKET_NUMBER = 1;
    private static final int MAX_TICKET_NUMBER_EXCLUSIVE = 21;

    private final RandomGenerator random;

    public TicketGenerator() {
        this(ThreadLocalRandom.current());
    }

    public TicketGenerator(RandomGenerator random) {
        this.random = Objects.requireNonNull(random);
    }

    public int generate() {
        return random.nextInt(MIN_TICKET_NUMBER, MAX_TICKET_NUMBER_EXCLUSIVE);
    }
}
