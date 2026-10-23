package com.poiesis.login;

import com.poiesis.login.security.AutenticacaoCredenciaisImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import static org.junit.jupiter.api.Assertions.*;

class AutenticacaoCredenciaisTests {
    @Test void emiteSomentePerfisDeNegocioMesmoComAutoridadesDeFator() {
        AuthenticationManager manager = credentials -> new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("FACTOR_PASSWORD"),
                    new SimpleGrantedAuthority("FACTOR_OTT")));
        var usuario = new AutenticacaoCredenciaisImpl(manager).autenticar("user@example.com", "senha");
        assertEquals("user@example.com", usuario.email());
        assertEquals(List.of("USER"), usuario.roles());
    }
}
