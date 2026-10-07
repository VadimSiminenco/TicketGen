package com.ticketgen.repository;

import com.ticketgen.domain.Student;
import java.util.List;
import java.util.Map;

public interface StudentRepository {
    Map<String, List<Student>> loadGroups();
}
