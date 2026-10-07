package com.ticketgen.repository;

import com.ticketgen.TestFiles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExcelStudentRepositoryTest {
    @TempDir Path directory;

    @Test
    void readsGroupsStudentsAndEmptySheetInOrder() throws Exception {
        TestFiles.students(directory);
        var groups = new ExcelStudentRepository(directory.resolve("students.xlsx")).loadGroups();
        assertEquals(java.util.List.of("TI-221", "TI-222", "EMPTY"), new java.util.ArrayList<>(groups.keySet()));
        assertEquals(1, groups.get("TI-221").size());
        assertEquals("Ivanov", groups.get("TI-221").get(0).lastName());
        assertEquals("Ivan", groups.get("TI-221").get(0).firstName());
        assertEquals("Petrov", groups.get("TI-222").get(0).lastName());
        assertTrue(groups.get("EMPTY").isEmpty());
    }
}
