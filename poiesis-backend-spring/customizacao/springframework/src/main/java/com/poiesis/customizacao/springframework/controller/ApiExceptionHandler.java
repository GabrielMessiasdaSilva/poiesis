package com.poiesis.customizacao.springframework.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception exception) {
        String message = exception instanceof MethodArgumentNotValidException validationException
                ? validationException.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .findFirst().orElse("Requisição inválida.")
                : exception.getMessage();
        return ResponseEntity.badRequest().body(Map.of("erro", message == null ? "Requisição inválida." : message));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, String>> handleCatalogFailure(FeignException exception) {
        HttpStatus status = exception.status() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.BAD_GATEWAY;
        String message = exception.status() == 404
                ? "Produto não encontrado no catálogo."
                : "Não foi possível consultar o serviço de catálogo.";
        return ResponseEntity.status(status).body(Map.of("erro", message));
    }
}
