package com.poiesis.relatorio.config;

import com.poiesis.relatorio.integration.ProducaoIntegration;
import com.poiesis.relatorio.repository.VendasRepository;
import com.poiesis.relatorio.service.RegistroVendaService;
import com.poiesis.relatorio.service.RelatorioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public RelatorioService relatorioService(VendasRepository vendas, ProducaoIntegration producao) {
        return new RelatorioService(vendas, producao);
    }
    @Bean
    public RegistroVendaService registrarVenda(VendasRepository vendas) {
        return new RegistroVendaService(vendas);
    }
}