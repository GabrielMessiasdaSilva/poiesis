package com.poiesis.login.repository;

import java.time.Instant;

public interface SessaoRepository {
    boolean revogada(String token);
    void revogar(String token, Instant expiresAt);
}
