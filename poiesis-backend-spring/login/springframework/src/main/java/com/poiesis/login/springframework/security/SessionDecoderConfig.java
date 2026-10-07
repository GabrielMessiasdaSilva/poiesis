package com.poiesis.login.springframework.security;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
@Configuration
public class SessionDecoderConfig {
    @Bean JwtDecoder jwtDecoder(Environment env, TokenSessions sessions, UserDetailsService users) {
        var key = new SecretKeySpec(env.getRequiredProperty("application.jwt.secret")
            .getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefault(), new JwtTimestampValidator(java.time.Duration.ZERO),
            new JwtClaimValidator<java.time.Instant>("exp", java.util.Objects::nonNull)));
        return token -> {
            Jwt jwt = decoder.decode(token);
            if (sessions.isRevoked(token)) throw new BadJwtException("Sessão encerrada.");
            try {
                var user = users.loadUserByUsername(jwt.getSubject());
                var currentRoles = user.getAuthorities().stream().map(a -> a.getAuthority().replace("ROLE_", "")).sorted().toList();
                var roles = jwt.getClaimAsStringList("roles");
                if (!user.isEnabled() || roles == null || !currentRoles.equals(roles.stream().sorted().toList()))
                    throw new BadJwtException("Sessão inválida.");
            } catch (UsernameNotFoundException e) { throw new BadJwtException("Sessão inválida.", e); }
            return jwt;
        };
    }
}
