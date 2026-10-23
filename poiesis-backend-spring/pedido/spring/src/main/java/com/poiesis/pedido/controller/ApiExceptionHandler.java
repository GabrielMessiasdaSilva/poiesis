package com.poiesis.pedido.controller;

import java.util.Map;
import org.springframework.amqp.AmqpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<?> upstreamResponse(RestClientResponseException e) {
        if (e.getStatusCode().value() == 400 || e.getStatusCode().value() == 404) return ResponseEntity.badRequest().body(Map.of("message", "Produto não encontrado no catálogo."));
        return ResponseEntity.status(503).body(Map.of("message", "Catálogo ou customizações indisponíveis."));
    }
    @ExceptionHandler({ResourceAccessException.class, AmqpException.class})
    public ResponseEntity<?> unavailable(Exception e) {
        return ResponseEntity.status(503).body(Map.of("message", "Serviço necessário ao pedido está indisponível."));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
