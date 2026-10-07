package com.poiesis.relatorio.springframework.repository;

import com.poiesis.relatorio.domain.entity.MetricaVendas;
import com.poiesis.relatorio.domain.entity.RelatorioProducao;
import com.poiesis.relatorio.domain.repository.RelatorioRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class RelatorioPersistenceAdapter implements RelatorioRepository {

    private final SpringDataRelatorioRepository repository;
    private final org.springframework.web.client.RestClient producao;

    public RelatorioPersistenceAdapter(SpringDataRelatorioRepository repository, org.springframework.core.env.Environment env) {
        this.repository = repository;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(5000);
        producao = org.springframework.web.client.RestClient.builder()
                .baseUrl(env.getProperty("application.producao.url", "http://localhost:8085"))
                .requestFactory(factory).build();
    }

    @Override
    public MetricaVendas obterMetricasVendas(LocalDate dataInicio, LocalDate dataFim) {
        Long totalPedidos = repository.sumTotalPedidosBetween(dataInicio, dataFim);
        BigDecimal faturamentoTotal = repository.sumFaturamentoTotalBetween(dataInicio, dataFim);

        return new MetricaVendas(
                totalPedidos != null ? totalPedidos : 0L,
                faturamentoTotal != null ? faturamentoTotal : BigDecimal.ZERO
        );
    }

    @Override
    public RelatorioProducao obterRelatorioProducao() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        var jwt = (org.springframework.security.oauth2.jwt.Jwt) auth.getPrincipal();
        try {
            var result = producao.get().uri("/v1/producao/resumo").headers(h -> h.setBearerAuth(jwt.getTokenValue()))
                    .retrieve().body(RelatorioProducao.class);
            if (result == null) throw new IllegalStateException("Resposta vazia da produção");
            return result;
        } catch (org.springframework.web.client.RestClientException | IllegalStateException e) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível consultar os dados de produção.", e);
        }
    }
}