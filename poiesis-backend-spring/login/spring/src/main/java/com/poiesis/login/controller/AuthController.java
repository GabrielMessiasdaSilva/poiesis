package com.poiesis.login.controller;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.service.AutenticacaoService;
import com.poiesis.login.service.CadastroUsuarioService;
import com.poiesis.login.service.SessaoService;
import com.poiesis.login.controller.adapter.UsuarioAdapterController;
import com.poiesis.login.controller.dto.request.LoginRequest;
import com.poiesis.login.controller.dto.request.RegisterRequest;
import com.poiesis.login.controller.dto.response.AuthResponse;
import com.poiesis.login.controller.dto.response.UsuarioResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final CadastroUsuarioService cadastrarUsuario;
    private final AutenticacaoService autenticarUsuario;
    private final SessaoService sessaoService;
    private final SessionCookie sessionCookie;

    public AuthController(CadastroUsuarioService cadastrarUsuario,
            AutenticacaoService autenticarUsuario,
            SessaoService sessaoService, SessionCookie sessionCookie) {
        this.cadastrarUsuario = cadastrarUsuario;
        this.autenticarUsuario = autenticarUsuario;
        this.sessaoService = sessaoService;
        this.sessionCookie = sessionCookie;
    }

    @GetMapping("/session")
    public Map<String, Object> session(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") List<String> roles,
            @RequestHeader("X-Token-Expires-At") long expiresAt) {
        return Map.of("email", email, "roles", roles, "expiresAt", expiresAt);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Token-Expires-At") long expiresAt) {
        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7) || authorization.length() <= 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        sessaoService.encerrar(authorization.substring(7), Instant.ofEpochMilli(expiresAt));
        return ResponseEntity.noContent().header("Set-Cookie", sessionCookie.clear()).build();
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegisterRequest requestDTO) {
        Usuario salvo = cadastrarUsuario.cadastrar(UsuarioAdapterController.toDomain(requestDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioAdapterController.toResponse(salvo));
    }

    @PostMapping("/login")
    public ResponseEntity<?> autenticar(@RequestBody LoginRequest requestDTO,
            @RequestHeader(value = "X-Auth-Mode", defaultValue = "bearer") String mode) {
        String token = autenticarUsuario.autenticar(requestDTO.getEmail(), requestDTO.getSenha());
        if ("cookie".equals(mode)) {
            return ResponseEntity.ok().header("Set-Cookie", sessionCookie.create(token))
                    .header("Cache-Control", "no-store").body(Map.of("tipo", "Cookie"));
        }
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(new AuthResponse(token));
    }
}
