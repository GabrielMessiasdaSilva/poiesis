package com.poiesis.relatorio.integration;

import com.poiesis.relatorio.entity.RelatorioProducao;
import com.poiesis.relatorio.integration.ProducaoIntegration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProducaoIntegrationImpl implements ProducaoIntegration {
    private final RestClient producao;

    public ProducaoIntegrationImpl(Environment env) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        producao = RestClient.builder().baseUrl(env.getProperty("application.producao.url", "http://localhost:8085"))
                .requestFactory(factory).build();
    }

    @Override
    public RelatorioProducao consultarResumo() {
        try {
            var result = producao.get().uri("/v1/producao/resumo")
                    .retrieve().body(RelatorioProducao.class);
            if (result == null) throw new IllegalStateException("Resposta vazia da produção");
            return result;
        } catch (RestClientException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível consultar os dados de produção.", e);
        }
    }
}
