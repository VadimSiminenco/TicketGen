package com.ticketgen.repository;

import com.ticketgen.domain.StudentRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class ExcelJournalRepository implements JournalRepository {
    static final String SHEET_NAME = "Journal";
    static final String[] HEADERS = {
            "Last name",
            "First name",
            "Номер билета",
            "Дата и время"
    };

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final Path journalPath;

    public ExcelJournalRepository(Path journalPath) {
        this.journalPath = Objects.requireNonNull(journalPath).toAbsolutePath().normalize();
    }

    @Override
    public void append(StudentRecord record) throws IOException {
        Objects.requireNonNull(record);

        try (XSSFWorkbook workbook = openWorkbook()) {
            Sheet sheet = getOrCreateSheet(workbook);
            ensureHeader(sheet);
            appendRow(sheet, record);
            saveAtomically(workbook);
        }
    }

    private XSSFWorkbook openWorkbook() throws IOException {
        if (Files.notExists(journalPath)) {
            return new XSSFWorkbook();
        }

        try (InputStream input = Files.newInputStream(journalPath)) {
            return new XSSFWorkbook(input);
        }
    }

    private Sheet getOrCreateSheet(XSSFWorkbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            return workbook.createSheet(SHEET_NAME);
        }
        return workbook.getSheetAt(0);
    }

    private void ensureHeader(Sheet sheet) {
        if (sheet.getRow(0) != null) {
            return;
        }

        Row header = sheet.createRow(0);
        for (int column = 0; column < HEADERS.length; column++) {
            header.createCell(column).setCellValue(HEADERS[column]);
        }
    }

    private void appendRow(Sheet sheet, StudentRecord record) {
        int rowIndex = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(record.lastName());
        row.createCell(1).setCellValue(record.firstName());
        row.createCell(2).setCellValue(record.ticketNumber());
        row.createCell(3).setCellValue(record.createdAt().format(DATE_TIME_FORMATTER));
    }

    private void saveAtomically(XSSFWorkbook workbook) throws IOException {
        Path parentDirectory = journalPath.getParent();
        Path temporaryFile = Files.createTempFile(parentDirectory, ".journal-", ".xlsx");

        try {
            writeAndFlush(workbook, temporaryFile);
            replaceJournal(temporaryFile);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private void writeAndFlush(XSSFWorkbook workbook, Path temporaryFile) throws IOException {
        try (FileChannel channel = FileChannel.open(
                temporaryFile,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
        ); OutputStream output = Channels.newOutputStream(channel)) {
            workbook.write(output);
            output.flush();
            channel.force(true);
        }
    }

    private void replaceJournal(Path temporaryFile) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    journalPath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException error) {
            Files.move(temporaryFile, journalPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
