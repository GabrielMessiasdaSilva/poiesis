package com.poiesis.login.security;

import com.poiesis.login.entity.UsuarioAutenticado;
import com.poiesis.login.security.EmissorToken;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class EmissorTokenJwt implements EmissorToken {

    private final Environment environment;

    public EmissorTokenJwt(Environment environment) {
        this.environment = environment;
    }

    private Key getSigningKey() {
        String secret = environment.getRequiredProperty("application.jwt.secret");
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String emitir(UsuarioAutenticado usuario) {
        long expiration = environment.getRequiredProperty("application.jwt.expiration", Long.class);

        // A assinatura permite verificar a integridade; a expiração limita a validade do acesso.
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(usuario.email())
                .claim("roles", usuario.roles())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
