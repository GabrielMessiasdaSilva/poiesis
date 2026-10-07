package com.poiesis.producao.springframework.config;

import com.poiesis.producao.domain.repository.OrdemProducaoRepository;
import com.poiesis.producao.domain.service.ProducaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ProducaoService producaoService(OrdemProducaoRepository repository) {
        return new ProducaoService(repository);
    }
}
