package com.ticketgen.service;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketGeneratorTest {
    @Test
    void generatedTicketIsNeverLessThanOne() {
        TicketGenerator generator = new TicketGenerator(new Random(1));

        for (int attempt = 0; attempt < 100_000; attempt++) {
            assertTrue(generator.generate() >= 1);
        }
    }

    @Test
    void generatedTicketIsNeverGreaterThanTwenty() {
        TicketGenerator generator = new TicketGenerator(new Random(2));

        for (int attempt = 0; attempt < 100_000; attempt++) {
            assertTrue(generator.generate() <= 20);
        }
    }
}
