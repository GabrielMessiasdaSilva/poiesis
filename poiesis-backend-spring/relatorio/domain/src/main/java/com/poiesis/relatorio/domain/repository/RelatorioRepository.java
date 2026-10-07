package com.poiesis.relatorio.domain.repository;

import com.poiesis.relatorio.domain.entity.MetricaVendas;
import com.poiesis.relatorio.domain.entity.RelatorioProducao;

import java.time.LocalDate;

public interface RelatorioRepository {
    MetricaVendas obterMetricasVendas(LocalDate dataInicio, LocalDate dataFim);
    RelatorioProducao obterRelatorioProducao();
}   