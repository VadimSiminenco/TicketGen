package com.ticketgen;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SourceAvailabilityIT {
    private static final Path DATA;
    static {
        try { DATA = Files.createTempDirectory("ticketgen-missing-"); }
        catch (Exception error) { throw new ExceptionInInitializerError(error); }
    }

    @DynamicPropertySource
    static void dataDirectory(DynamicPropertyRegistry registry) {
        registry.add("ticketgen.data-directory", DATA::toString);
    }

    @LocalServerPort int port;

    @Test
    void missingSourcesShowMessagesWithoutStoppingServer() throws Exception {
        String empty = home();
        assertTrue(empty.contains("Не найден файл students.xlsx"));
        assertTrue(empty.contains("Не найден файл tickets.docx"));
        assertTrue(empty.contains("disabled"));

        TestFiles.students(DATA);
        String missingTickets = home();
        assertTrue(missingTickets.contains("Не найден файл tickets.docx"));

        Files.delete(DATA.resolve("students.xlsx"));
        TestFiles.tickets(DATA);
        String missingStudents = home();
        assertTrue(missingStudents.contains("Не найден файл students.xlsx"));
    }

    private String home() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/")).GET().build();
        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return response.body();
    }
}
