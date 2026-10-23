package com.poiesis.login.controller;

import com.poiesis.login.exception.EmailAlreadyRegisteredException;
import com.poiesis.login.exception.SessaoInvalidaException;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler({EmailAlreadyRegisteredException.class,
            DataIntegrityViolationException.class})
    public ResponseEntity<?> duplicateEmail(Exception e) {
        return ResponseEntity.status(409).body(Map.of("message", "E-mail já cadastrado no sistema."));
    }
    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<?> invalidRegistration(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", "Dados de cadastro inválidos."));
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> invalidCredentials(AuthenticationException e) {
        return ResponseEntity.status(401).body(Map.of("message", "Credenciais inválidas."));
    }
    @ExceptionHandler(SessaoInvalidaException.class)
    public ResponseEntity<?> invalidSession(Exception e) {
        return ResponseEntity.status(401).body(Map.of("message", "Sessão inválida."));
    }
}
