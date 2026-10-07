package com.ticketgen.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class DataPaths {
    private final Path directory;

    public DataPaths(@Value("${ticketgen.data-directory:.}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public Path students() { return directory.resolve("students.xlsx"); }
    public Path tickets() { return directory.resolve("tickets.docx"); }
    public Path results() { return directory.resolve("results.xlsx"); }
}
