package com.poiesis.relatorio.springframework.config;

import com.poiesis.relatorio.domain.repository.RelatorioRepository;
import com.poiesis.relatorio.domain.service.RelatorioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public RelatorioService relatorioService(RelatorioRepository relatorioRepository) {
        return new RelatorioService(relatorioRepository);
    }
}