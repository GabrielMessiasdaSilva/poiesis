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

    public RelatorioPersistenceAdapter(SpringDataRelatorioRepository repository) {
        this.repository = repository;
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
        // Exemplo consolidado (pode consultar visão do banco local ou caches pré-computados)
        return new RelatorioProducao(10L, 5L, 8L, 3L, 42L);
    }
}