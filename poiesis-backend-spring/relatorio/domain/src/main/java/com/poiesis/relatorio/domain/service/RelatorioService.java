package com.poiesis.relatorio.domain.service;

import com.poiesis.relatorio.domain.entity.MetricaVendas;
import com.poiesis.relatorio.domain.entity.RelatorioProducao;
import com.poiesis.relatorio.domain.repository.RelatorioRepository;

import java.time.LocalDate;

public class RelatorioService {

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public MetricaVendas gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
        }
        return relatorioRepository.obterMetricasVendas(dataInicio, dataFim);
    }

    public RelatorioProducao gerarRelatorioProducao() {
        return relatorioRepository.obterRelatorioProducao();
    }
}