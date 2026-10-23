package com.poiesis.login.repository.jpa;

import com.poiesis.login.repository.orm.UsuarioOrmJpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositoryWithJpa extends JpaRepository<UsuarioOrmJpa, Long> {
    Optional<UsuarioOrmJpa> findByEmail(String email);
    boolean existsByEmail(String email);
}