package com.poiesis.login.springframework.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler({com.poiesis.login.domain.service.EmailAlreadyRegisteredException.class,
            org.springframework.dao.DataIntegrityViolationException.class})
    public ResponseEntity<?> duplicateEmail(Exception e) {
        return ResponseEntity.status(409).body(java.util.Map.of("message", "E-mail já cadastrado no sistema."));
    }
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<?> invalidRegistration(Exception e) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", "Dados de cadastro inválidos."));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> invalidCredentials(AuthenticationException e) {
        return ResponseEntity.status(401).body(java.util.Map.of("message", "Credenciais inválidas."));
    }
}
