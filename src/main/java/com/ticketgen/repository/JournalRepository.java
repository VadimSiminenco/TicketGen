package com.ticketgen.repository;

import com.ticketgen.domain.StudentRecord;

import java.io.IOException;

public interface JournalRepository {
    void append(StudentRecord record) throws IOException;
}
