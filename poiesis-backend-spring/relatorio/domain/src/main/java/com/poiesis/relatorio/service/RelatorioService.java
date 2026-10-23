package com.poiesis.relatorio.service;

import com.poiesis.relatorio.entity.MetricaVendas;
import com.poiesis.relatorio.entity.RelatorioProducao;
import com.poiesis.relatorio.integration.ProducaoIntegration;
import com.poiesis.relatorio.repository.VendasRepository;
import java.time.LocalDate;

public class RelatorioService {

    private final VendasRepository vendas;
    private final ProducaoIntegration producao;

    public RelatorioService(VendasRepository vendas, ProducaoIntegration producao) {
        this.vendas = vendas;
        this.producao = producao;
    }

    public MetricaVendas gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        return vendas.obterMetricasVendas(dataInicio, dataFim);
    }

    public RelatorioProducao gerarRelatorioProducao() {
        return producao.consultarResumo();
    }
}