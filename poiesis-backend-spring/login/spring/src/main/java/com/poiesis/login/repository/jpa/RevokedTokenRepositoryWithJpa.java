package com.poiesis.login.repository.jpa;

import com.poiesis.login.repository.orm.RevokedTokenOrmJpa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepositoryWithJpa extends JpaRepository<RevokedTokenOrmJpa, String> {}
