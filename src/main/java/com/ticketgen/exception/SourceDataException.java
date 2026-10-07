package com.ticketgen.exception;

public class SourceDataException extends RuntimeException {
    public SourceDataException(String message, Throwable cause) { super(message, cause); }
    public SourceDataException(String message) { super(message); }
}
