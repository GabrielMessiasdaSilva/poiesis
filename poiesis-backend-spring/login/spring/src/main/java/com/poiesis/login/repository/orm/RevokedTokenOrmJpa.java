package com.poiesis.login.repository.orm;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "tb_revoked_tokens")
public class RevokedTokenOrmJpa {
    @Id private String tokenHash;
    private Instant expiresAt;
    protected RevokedTokenOrmJpa() {}
    public RevokedTokenOrmJpa(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
