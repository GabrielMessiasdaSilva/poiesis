package com.poiesis.login.controller;

import com.poiesis.login.service.SessaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Endpoint privado, sem rota no gateway. Recebe apenas claims já validadas por ele. */
@RestController
@RequestMapping("/internal/sessions")
public class InternalSessionController {
    private final SessaoService sessaoService;

    public InternalSessionController(SessaoService sessaoService) { this.sessaoService = sessaoService; }

    @PostMapping("/validate")
    public ResponseEntity<Void> validate(@Valid @RequestBody SessionCheck check) {
        sessaoService.validar(check.token(), check.email(), check.roles());
        return ResponseEntity.noContent().build();
    }

    public record SessionCheck(@NotBlank String token, @NotBlank String email,
                               @NotEmpty List<@NotBlank String> roles) {}
}
