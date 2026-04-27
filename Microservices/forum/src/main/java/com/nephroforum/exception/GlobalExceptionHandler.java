package com.nephroforum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ErrorResponse(int status, String error, Object details) {}

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(404)
                .body(new ErrorResponse(404, "Not Found", e.getMessage()));
    }

    @ExceptionHandler(ContentModeratedException.class)
    public ResponseEntity<ErrorResponse> handleModeration(ContentModeratedException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Content Moderated", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(SecurityException e) {
        return ResponseEntity.status(403)
                .body(new ErrorResponse(403, "Forbidden", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors())
            errors.put(fe.getField(), fe.getDefaultMessage());
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, "Validation Failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "Internal Server Error", e.getMessage()));
    }
}