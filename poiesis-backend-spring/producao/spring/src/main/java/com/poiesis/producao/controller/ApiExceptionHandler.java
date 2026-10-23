package com.poiesis.producao.controller;

import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> concurrentUpdate() {
        return ResponseEntity.status(409).body(Map.of("message",
                "Esta ordem foi alterada por outra operação. Atualize a consulta antes de salvar novamente."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Map.of("message",
                error.getMessage() == null ? "Requisição inválida." : error.getMessage()));
    }
}
