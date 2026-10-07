package com.ticketgen;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestFiles {
    private TestFiles() {}

    public static void students(Path directory) throws IOException {
        try (XSSFWorkbook book = new XSSFWorkbook()) {
            var first = book.createSheet("TI-221");
            var header = first.createRow(0);
            header.createCell(0).setCellValue("Last name");
            header.createCell(1).setCellValue("First name");
            var row = first.createRow(1);
            row.createCell(0).setCellValue("  Ivanov ");
            row.createCell(1).setCellValue(" Ivan  ");
            first.createRow(2);
            var malformed = first.createRow(3);
            malformed.createCell(0).setCellValue("Bad");
            var second = book.createSheet("TI-222");
            var other = second.createRow(1);
            other.createCell(0).setCellValue("Petrov");
            other.createCell(1).setCellValue("Petr");
            book.createSheet("EMPTY");
            try (OutputStream output = Files.newOutputStream(directory.resolve("students.xlsx"))) {
                book.write(output);
            }
        }
    }

    public static void tickets(Path directory) throws IOException {
        try (XWPFDocument document = new XWPFDocument()) {
            for (String line : new String[]{
                    "Билет 7", "1. Первый вопрос", "2. Второй вопрос", "3. Третий вопрос",
                    "Билет 8", "1. Короткий", "2. Недостаточно",
                    "Билет 15", "1. Вопрос A", "2. Вопрос B", "3. Вопрос C", "4. Дополнительный"
            }) {
                document.createParagraph().createRun().setText(line);
            }
            try (OutputStream output = Files.newOutputStream(directory.resolve("tickets.docx"))) {
                document.write(output);
            }
        }
    }
}
