package com.ticketgen.controller;

import com.ticketgen.domain.GenerationResult;
import com.ticketgen.domain.Student;
import com.ticketgen.exception.ResultFileLockedException;
import com.ticketgen.exception.ResultStorageException;
import com.ticketgen.exception.SourceDataException;
import com.ticketgen.service.TicketAssignmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketApiController {
    private static final Logger log = LoggerFactory.getLogger(TicketApiController.class);
    private final TicketAssignmentService service;

    public TicketApiController(TicketAssignmentService service) { this.service = service; }

    @GetMapping("/students")
    public List<StudentOption> students(@RequestParam String group) {
        List<Student> inGroup = service.groups().get(group);
        if (inGroup == null) throw new IllegalArgumentException("Неизвестная группа.");
        return inGroup.stream().map(student -> new StudentOption(student.key(), student.label())).toList();
    }

    @PostMapping("/generate")
    public GenerationResult generate(@RequestBody GenerateRequest request) {
        return service.generate(request.group(), request.studentKey());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> invalidSelection(IllegalArgumentException problem) {
        return ResponseEntity.badRequest().body(new ApiError("invalid", problem.getMessage()));
    }

    @ExceptionHandler(SourceDataException.class)
    public ResponseEntity<ApiError> sourceUnavailable(SourceDataException problem) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("source", problem.getMessage()));
    }

    @ExceptionHandler(ResultFileLockedException.class)
    public ResponseEntity<ApiError> locked(ResultFileLockedException problem) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(new ApiError("locked", "Файл results.xlsx сейчас открыт в Excel. Закройте файл и повторите сохранение."));
    }

    @ExceptionHandler(ResultStorageException.class)
    public ResponseEntity<ApiError> storageFailure(ResultStorageException problem) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("storage", "Не удалось сохранить результат. Проверьте доступ к results.xlsx."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> unexpected(RuntimeException problem) {
        log.error("Unexpected request failure", problem);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError("internal", "Внутренняя ошибка. Повторите попытку позже."));
    }

    public record StudentOption(String key, String label) {}
    public record GenerateRequest(String group, String studentKey) {}
    public record ApiError(String code, String message) {}
}
