package com.ticketgen.repository;

import com.ticketgen.domain.ResultEntry;
import com.ticketgen.domain.Student;
import java.util.OptionalInt;

public interface ResultRepository {
    OptionalInt firstTicketFor(Student student);
    void append(ResultEntry entry);
}
