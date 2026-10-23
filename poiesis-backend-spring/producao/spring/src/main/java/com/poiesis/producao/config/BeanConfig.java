package com.poiesis.producao.config;

import com.poiesis.producao.repository.OrdemProducaoRepository;
import com.poiesis.producao.repository.ResumoProducaoRepository;
import com.poiesis.producao.messaging.ProducaoPublisher;
import com.poiesis.producao.service.ProducaoService;
import com.poiesis.producao.service.ResumoProducaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ProducaoService producaoService(OrdemProducaoRepository repository, ProducaoPublisher publisher) {
        return new ProducaoService(repository, publisher);
    }
    @Bean
    public ResumoProducaoService consultarResumoProducao(
            ResumoProducaoRepository resumo) {
        return new ResumoProducaoService(resumo);
    }
}
