package com.poiesis.relatorio.domain.entity;

public record RelatorioProducao(
        Long emPendente,
        Long emCorte,
        Long emCostura,
        Long emAcabamento,
        Long concluidos
) {}
