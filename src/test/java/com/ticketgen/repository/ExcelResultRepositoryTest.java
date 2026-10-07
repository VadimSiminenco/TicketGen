package com.ticketgen.repository;

import com.ticketgen.domain.ResultEntry;
import com.ticketgen.domain.Student;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExcelResultRepositoryTest {
    @TempDir Path directory;
    private final Student student = new Student("TI-221", "Ivanov", "Ivan");

    @Test
    void createsHeaderAppendsAndKeepsFirstHistoricalTicket() throws Exception {
        Path path = directory.resolve("results.xlsx");
        ExcelResultRepository repository = new ExcelResultRepository(path);
        repository.append(entry(7, false));
        repository.append(entry(15, true));
        repository = new ExcelResultRepository(path);
        assertEquals(7, repository.firstTicketFor(student).orElseThrow());
        try (var input = Files.newInputStream(path); XSSFWorkbook book = new XSSFWorkbook(input)) {
            var sheet = book.getSheetAt(0);
            assertEquals(2, sheet.getLastRowNum());
            for (int i = 0; i < ExcelResultRepository.HEADERS.length; i++) {
                assertEquals(ExcelResultRepository.HEADERS[i], sheet.getRow(0).getCell(i).getStringCellValue());
            }
            assertEquals(7, (int) sheet.getRow(1).getCell(3).getNumericCellValue());
            assertEquals("нет", sheet.getRow(1).getCell(5).getStringCellValue());
            assertEquals(15, (int) sheet.getRow(2).getCell(3).getNumericCellValue());
            assertEquals("да", sheet.getRow(2).getCell(5).getStringCellValue());
        }
    }

    private ResultEntry entry(int number, boolean repeat) {
        return new ResultEntry(student, number, LocalDateTime.of(2026, 10, 7, 12, 0), repeat);
    }

    @Test
    void lockedFileCanBeRetriedWithoutDuplicateRowOnWindows() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getProperty("os.name").toLowerCase().contains("win"));
        Path path = directory.resolve("results.xlsx");
        ExcelResultRepository repository = new ExcelResultRepository(path);
        repository.append(entry(7, false));
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
             FileLock ignored = channel.lock()) {
            assertThrows(com.ticketgen.exception.ResultFileLockedException.class,
                    () -> repository.append(entry(7, true)));
        }
        repository.append(entry(7, true));
        try (var input = Files.newInputStream(path); XSSFWorkbook book = new XSSFWorkbook(input)) {
            assertEquals(2, book.getSheetAt(0).getLastRowNum());
            assertEquals("нет", book.getSheetAt(0).getRow(1).getCell(5).getStringCellValue());
            assertEquals("да", book.getSheetAt(0).getRow(2).getCell(5).getStringCellValue());
        }
    }
}
