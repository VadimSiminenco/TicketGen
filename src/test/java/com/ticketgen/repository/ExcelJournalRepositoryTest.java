package com.ticketgen.repository;

import com.ticketgen.domain.StudentRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelJournalRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void newJournalContainsHeaderAndFirstRecord() throws Exception {
        Path journal = temporaryDirectory.resolve("journal.xlsx");
        ExcelJournalRepository repository = new ExcelJournalRepository(journal);

        repository.append(record("Ivanov", "Ivan", 7, 10, 45, 23));

        try (XSSFWorkbook workbook = open(journal)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            for (int column = 0; column < ExcelJournalRepository.HEADERS.length; column++) {
                assertEquals(
                        ExcelJournalRepository.HEADERS[column],
                        header.getCell(column).getStringCellValue()
                );
            }
            assertEquals("Ivanov", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Ivan", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals(7, (int) sheet.getRow(1).getCell(2).getNumericCellValue());
            assertEquals("07.10.2026 10:45:23", sheet.getRow(1).getCell(3).getStringCellValue());
        }
    }

    @Test
    void newRecordIsAddedAfterExistingRecord() throws Exception {
        Path journal = temporaryDirectory.resolve("journal.xlsx");
        ExcelJournalRepository repository = new ExcelJournalRepository(journal);
        repository.append(record("Ivanov", "Ivan", 7, 10, 45, 23));

        repository.append(record("Petrov", "Petr", 15, 10, 46, 2));

        try (XSSFWorkbook workbook = open(journal)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(2, sheet.getLastRowNum());
            assertEquals("Petrov", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Petr", sheet.getRow(2).getCell(1).getStringCellValue());
        }
    }

    @Test
    void reopeningJournalDoesNotRemoveExistingRecords() throws Exception {
        Path journal = temporaryDirectory.resolve("journal.xlsx");
        new ExcelJournalRepository(journal)
                .append(record("Ivanov", "Ivan", 7, 10, 45, 23));

        new ExcelJournalRepository(journal)
                .append(record("Sidorov", "Alex", 20, 10, 47, 10));

        try (XSSFWorkbook workbook = open(journal)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Ivanov", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Ivan", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Sidorov", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Alex", sheet.getRow(2).getCell(1).getStringCellValue());
        }
    }

    private XSSFWorkbook open(Path journal) throws Exception {
        InputStream input = Files.newInputStream(journal);
        return new XSSFWorkbook(input);
    }

    private StudentRecord record(
            String lastName,
            String firstName,
            int ticketNumber,
            int hour,
            int minute,
            int second
    ) {
        return new StudentRecord(
                lastName,
                firstName,
                ticketNumber,
                LocalDateTime.of(2026, 10, 7, hour, minute, second)
        );
    }
}
