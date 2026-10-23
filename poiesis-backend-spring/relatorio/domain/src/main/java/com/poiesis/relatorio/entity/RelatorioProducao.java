package com.poiesis.relatorio.entity;

public record RelatorioProducao(
        Long emPendente,
        Long emCorte,
        Long emCostura,
        Long emAcabamento,
        Long concluidos
) {}
