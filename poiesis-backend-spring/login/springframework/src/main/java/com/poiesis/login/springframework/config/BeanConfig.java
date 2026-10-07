package com.poiesis.login.springframework.config;

import com.poiesis.login.domain.repository.UsuarioRepositoryPort;
import com.poiesis.login.domain.service.UsuarioDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UsuarioDomainService usuarioDomainService(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new UsuarioDomainService(usuarioRepositoryPort);
    }
}