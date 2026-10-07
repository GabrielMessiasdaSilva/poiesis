package com.poiesis.login.springframework.repository.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
@Entity
@Table(name = "tb_revoked_tokens")
public class RevokedTokenEntity {
    @Id private String tokenHash;
    private Instant expiresAt;
    protected RevokedTokenEntity() {}
    public RevokedTokenEntity(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }
}
