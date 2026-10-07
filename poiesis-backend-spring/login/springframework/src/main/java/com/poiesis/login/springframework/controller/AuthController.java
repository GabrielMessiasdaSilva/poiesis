package com.poiesis.login.springframework.controller;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.domain.service.UsuarioDomainService;
import com.poiesis.login.springframework.controller.adapter.UsuarioMapper;
import com.poiesis.login.springframework.controller.dto.request.LoginRequestDTO;
import com.poiesis.login.springframework.controller.dto.request.RegisterRequestDTO;
import com.poiesis.login.springframework.controller.dto.response.AuthResponseDTO;
import com.poiesis.login.springframework.controller.dto.response.UsuarioResponseDTO;
import com.poiesis.login.springframework.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UsuarioDomainService usuarioDomainService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final com.poiesis.login.springframework.security.TokenSessions sessions;

    public AuthController(UsuarioDomainService usuarioDomainService,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService, com.poiesis.login.springframework.security.TokenSessions sessions) {
        this.usuarioDomainService = usuarioDomainService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.sessions = sessions;
    }

    @GetMapping("/session")
    public java.util.Map<String, Object> session(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        return java.util.Map.of("email", jwt.getSubject(), "roles", jwt.getClaimAsStringList("roles"),
                "expiresAt", jwt.getExpiresAt().toEpochMilli());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.oauth2.jwt.Jwt jwt) {
        sessions.revoke(jwt);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registrar(@jakarta.validation.Valid @RequestBody RegisterRequestDTO requestDTO) {
        requestDTO.setNome(requestDTO.getNome().trim());
        requestDTO.setEmail(requestDTO.getEmail().trim());
        // Criptografa a senha antes de salvar no domínio
        String senhaCriptografada = passwordEncoder.encode(requestDTO.getSenha());
        requestDTO.setSenha(senhaCriptografada);

        Usuario domain = UsuarioMapper.toDomain(requestDTO);
        Usuario salvo = usuarioDomainService.cadastrarNovoUsuario(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponse(salvo));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> autenticar(@RequestBody LoginRequestDTO requestDTO) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.getEmail(), requestDTO.getSenha())
        );

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String jwtToken = jwtService.gerarToken(userDetails);

        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }
}