package com.poiesis.login.springframework.security;
import com.poiesis.login.springframework.repository.RevokedTokenRepository;
import com.poiesis.login.springframework.repository.entity.RevokedTokenEntity;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.Jwt;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
@Service
public class TokenSessions {
    private final RevokedTokenRepository repository;
    public TokenSessions(RevokedTokenRepository repository) { this.repository = repository; }
    public boolean isRevoked(String token) { return repository.existsById(hash(token)); }
    public void revoke(Jwt jwt) {
        repository.save(new RevokedTokenEntity(hash(jwt.getTokenValue()), jwt.getExpiresAt()));
    }
    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
