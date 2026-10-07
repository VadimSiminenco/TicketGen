package com.ticketgen.repository;

import com.ticketgen.config.DataPaths;
import com.ticketgen.domain.Student;
import com.ticketgen.exception.SourceDataException;
import org.apache.poi.ss.usermodel.Cell;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ExcelStudentRepository implements StudentRepository {
    private static final Logger log = LoggerFactory.getLogger(ExcelStudentRepository.class);
    private final Path path;

    @Autowired
    public ExcelStudentRepository(DataPaths paths) { this.path = paths.students(); }
    public ExcelStudentRepository(Path path) { this.path = path; }

    @Override
    public Map<String, List<Student>> loadGroups() {
        if (!Files.isRegularFile(path)) {
            log.warn("Student source is missing: {}", path);
            throw new SourceDataException("Не найден файл students.xlsx. Добавьте его в папку приложения.");
        }
        Map<String, List<Student>> groups = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();
        try (InputStream input = Files.newInputStream(path);
             XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            for (Sheet sheet : workbook) {
                List<Student> students = new ArrayList<>();
                for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                    Row row = sheet.getRow(index);
                    if (row == null) continue;
                    String last = value(row.getCell(0), formatter);
                    String first = value(row.getCell(1), formatter);
                    if (last.isEmpty() && first.isEmpty()) continue;
                    if (last.isEmpty() || first.isEmpty()) {
                        log.warn("Skipping malformed student row {} in group {}", index + 1, sheet.getSheetName());
                        continue;
                    }
                    students.add(new Student(sheet.getSheetName(), last, first));
                }
                groups.put(sheet.getSheetName(), List.copyOf(students));
                log.info("Loaded group {}: {} students", sheet.getSheetName(), students.size());
            }
            log.info("Loaded {} student groups", groups.size());
            return groups;
        } catch (IOException | RuntimeException error) {
            log.error("Cannot read students.xlsx", error);
            throw new SourceDataException("Не удалось прочитать students.xlsx.", error);
        }
    }

    private String value(Cell cell, DataFormatter formatter) {
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
