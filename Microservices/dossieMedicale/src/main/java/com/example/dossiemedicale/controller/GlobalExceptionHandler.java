package com.example.dossiemedicale.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validationFailed(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation: {}", msg);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("message", msg.isEmpty() ? "Données invalides" : msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
        log.warn("Erreur IllegalArgumentException: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "BAD_REQUEST");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflictState(IllegalStateException ex) {
        log.warn("Erreur IllegalStateException: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICT");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> conflictDb(DataIntegrityViolationException ex) {
        log.error("Erreur DataIntegrityViolationException: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("error", "CONFLICT");
        
        String message = ex.getMostSpecificCause().getMessage();
        if (message.contains("foreign key")) {
            body.put("message", "La relation vers une constante ou autre entité n'existe pas.");
            body.put("details", message);
        } else if (message.contains("unique")) {
            body.put("message", "Un enregistrement unique a été violé.");
            body.put("details", message);
        } else {
            body.put("message", "Contrainte de base de données violée.");
            body.put("details", message);
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonError(HttpMessageNotReadableException ex) {
        log.warn("Erreur JSON: {}", ex.getMessage());
        Map<String, Object> body = new HashMap<>();
        body.put("error", "BAD_REQUEST");
        body.put("message", "Format JSON invalide. Vérifiez les champs de la requête.");
        body.put("details", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generalError(Exception ex) {
        log.error("Erreur générale: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INTERNAL_SERVER_ERROR");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
