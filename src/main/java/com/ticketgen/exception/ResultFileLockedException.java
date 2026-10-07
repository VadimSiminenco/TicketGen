package com.ticketgen.exception;

public class ResultFileLockedException extends ResultStorageException {
    public ResultFileLockedException(Throwable cause) { super("results.xlsx is locked", cause); }
}
