package com.poiesis.login.springframework.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> invalidCredentials(AuthenticationException e) {
        return ResponseEntity.status(401).body(java.util.Map.of("message", "Credenciais inválidas."));
    }
}
