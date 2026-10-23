package com.poiesis.produto.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> concurrentUpdate() {
        return ResponseEntity.status(409).body(Map.of("message",
                "Este registro foi alterado por outra operação. Atualize a consulta antes de salvar novamente."));
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> invalidRequest(Exception error, HttpServletRequest request) {
        String message = error instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                    .map(field -> field.getField() + ": " + field.getDefaultMessage())
                    .findFirst().orElse("Requisição inválida.")
                : error.getMessage();
        String key = request.getRequestURI().startsWith("/v1/customizacoes") ? "erro" : "message";
        return ResponseEntity.badRequest().body(Map.of(key, message == null ? "Requisição inválida." : message));
    }
}
