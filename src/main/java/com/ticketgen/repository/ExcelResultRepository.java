package com.ticketgen.repository;

import com.ticketgen.config.DataPaths;
import com.ticketgen.domain.ResultEntry;
import com.ticketgen.domain.Student;
import com.ticketgen.exception.ResultFileLockedException;
import com.ticketgen.exception.ResultStorageException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.OptionalInt;

@Repository
public class ExcelResultRepository implements ResultRepository {
    private static final Logger log = LoggerFactory.getLogger(ExcelResultRepository.class);
    public static final String[] HEADERS = {"Группа", "Фамилия", "Имя", "Номер билета", "Дата и время", "Повтор (да/нет)"};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final Path path;

    @Autowired
    public ExcelResultRepository(DataPaths paths) { this.path = paths.results(); }
    public ExcelResultRepository(Path path) { this.path = path.toAbsolutePath().normalize(); }

    @Override
    public OptionalInt firstTicketFor(Student student) {
        if (!Files.exists(path)) return OptionalInt.empty();
        try (XSSFWorkbook book = open()) {
            Sheet sheet = book.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                if (student.group().equals(text(row, 0, formatter))
                        && student.lastName().equals(text(row, 1, formatter))
                        && student.firstName().equals(text(row, 2, formatter))) {
                    return OptionalInt.of((int) row.getCell(3).getNumericCellValue());
                }
            }
            return OptionalInt.empty();
        } catch (IOException error) {
            log.warn("Cannot read results.xlsx; file may be locked: {}", error.getMessage());
            throw new ResultFileLockedException(error);
        } catch (RuntimeException error) {
            log.error("Cannot read results.xlsx", error);
            throw new ResultStorageException("Cannot read results.xlsx", error);
        }
    }

    @Override
    public void append(ResultEntry entry) {
        Path temporary = null;
        try (XSSFWorkbook book = open()) {
            Sheet sheet = book.getNumberOfSheets() == 0 ? book.createSheet("Results") : book.getSheetAt(0);
            if (sheet.getRow(0) == null) {
                Row header = sheet.createRow(0);
                for (int i = 0; i < HEADERS.length; i++) header.createCell(i).setCellValue(HEADERS[i]);
            }
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            row.createCell(0).setCellValue(entry.student().group());
            row.createCell(1).setCellValue(entry.student().lastName());
            row.createCell(2).setCellValue(entry.student().firstName());
            row.createCell(3).setCellValue(entry.ticketNumber());
            row.createCell(4).setCellValue(entry.createdAt().format(DATE_FORMAT));
            row.createCell(5).setCellValue(entry.repeat() ? "да" : "нет");

            temporary = Files.createTempFile(path.getParent(), ".results-", ".xlsx");
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                 OutputStream output = Channels.newOutputStream(channel)) {
                book.write(output);
                output.flush();
                channel.force(true);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Saved ticket {} for group {} student {} {}", entry.ticketNumber(),
                    entry.student().group(), entry.student().lastName(), entry.student().firstName());
        } catch (AccessDeniedException error) {
            log.warn("results.xlsx is locked", error);
            throw new ResultFileLockedException(error);
        } catch (FileSystemException error) {
            log.warn("Cannot replace results.xlsx; file may be locked", error);
            throw new ResultFileLockedException(error);
        } catch (IOException error) {
            if (temporary == null && Files.exists(path)) {
                log.warn("Cannot open results.xlsx; file may be locked: {}", error.getMessage());
                throw new ResultFileLockedException(error);
            }
            log.error("Cannot save results.xlsx", error);
            throw new ResultStorageException("Cannot save results.xlsx", error);
        } catch (RuntimeException error) {
            log.error("Cannot save results.xlsx", error);
            throw new ResultStorageException("Cannot save results.xlsx", error);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException error) { log.warn("Cannot remove temporary result file", error); }
            }
        }
    }

    private XSSFWorkbook open() throws IOException {
        if (!Files.exists(path)) return new XSSFWorkbook();
        try (InputStream input = Files.newInputStream(path)) { return new XSSFWorkbook(input); }
    }

    private String text(Row row, int column, DataFormatter formatter) {
        return row.getCell(column) == null ? "" : formatter.formatCellValue(row.getCell(column)).trim();
    }
}
