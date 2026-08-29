package com.example.SpringBoot_Bakend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " " + error.getDefaultMessage()).orElse("Invalid request.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException exception) { return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage())); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException exception) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage())); }
    @ExceptionHandler({SecurityException.class, BadCredentialsException.class})
    ResponseEntity<Map<String, String>> forbidden(Exception exception) { return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied.")); }
}
