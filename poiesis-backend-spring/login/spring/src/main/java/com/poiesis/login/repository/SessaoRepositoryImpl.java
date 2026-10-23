package com.poiesis.login.repository;

import com.poiesis.login.repository.SessaoRepository;
import com.poiesis.login.repository.jpa.RevokedTokenRepositoryWithJpa;
import com.poiesis.login.repository.orm.RevokedTokenOrmJpa;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

@Service
public class SessaoRepositoryImpl implements SessaoRepository {
    private final RevokedTokenRepositoryWithJpa repository;
    public SessaoRepositoryImpl(RevokedTokenRepositoryWithJpa repository) { this.repository = repository; }
    @Override public boolean revogada(String token) { return repository.existsById(hash(token)); }
    @Override public void revogar(String token, Instant expiresAt) {
        repository.save(new RevokedTokenOrmJpa(hash(token), expiresAt));
    }
    // Guarda apenas a impressão SHA-256 do token revogado, sem persistir o JWT completo.
    private String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
