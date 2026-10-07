package com.poiesis.login.springframework.repository;
import com.poiesis.login.springframework.repository.entity.RevokedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RevokedTokenRepository extends JpaRepository<RevokedTokenEntity, String> {}
