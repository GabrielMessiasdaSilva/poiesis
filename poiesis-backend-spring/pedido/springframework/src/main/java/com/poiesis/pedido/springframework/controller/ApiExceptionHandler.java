package com.poiesis.pedido.springframework.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(org.springframework.web.client.RestClientResponseException.class)
    public ResponseEntity<?> upstreamResponse(org.springframework.web.client.RestClientResponseException e) {
        if (e.getStatusCode().value() == 404) return ResponseEntity.badRequest().body(java.util.Map.of("message", "Produto não encontrado no catálogo."));
        return ResponseEntity.status(503).body(java.util.Map.of("message", "Catálogo ou customizações indisponíveis."));
    }
    @ExceptionHandler({org.springframework.web.client.ResourceAccessException.class, org.springframework.amqp.AmqpException.class})
    public ResponseEntity<?> unavailable(Exception e) {
        return ResponseEntity.status(503).body(java.util.Map.of("message", "Serviço necessário ao pedido está indisponível."));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
    }
}
