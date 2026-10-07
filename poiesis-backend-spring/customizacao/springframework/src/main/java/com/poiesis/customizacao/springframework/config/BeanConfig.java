package com.poiesis.customizacao.springframework.config;

import com.poiesis.customizacao.domain.repository.CustomizacaoRepositoryPort;
import com.poiesis.customizacao.domain.service.CustomizacaoDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CustomizacaoDomainService customizacaoDomainService(CustomizacaoRepositoryPort customizacaoRepositoryPort) {
        return new CustomizacaoDomainService(customizacaoRepositoryPort);
    }
}