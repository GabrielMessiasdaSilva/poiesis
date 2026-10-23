package com.poiesis.login.security;

import com.poiesis.login.entity.UsuarioAutenticado;
import com.poiesis.login.security.AutenticacaoCredenciais;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AutenticacaoCredenciaisImpl implements AutenticacaoCredenciais {
    private final AuthenticationManager authentication;

    public AutenticacaoCredenciaisImpl(AuthenticationManager authentication) { this.authentication = authentication; }

    @Override
    public UsuarioAutenticado autenticar(String email, String senha) {
        var result = authentication.authenticate(new UsernamePasswordAuthenticationToken(email, senha));
        // Autoridades técnicas de autenticação (como FACTOR_PASSWORD) não são perfis de negócio.
        var roles = result.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .toList();
        return new UsuarioAutenticado(result.getName(), roles);
    }
}
