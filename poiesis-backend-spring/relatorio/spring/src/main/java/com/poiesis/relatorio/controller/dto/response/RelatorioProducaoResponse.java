package com.poiesis.relatorio.controller.dto.response;

public record RelatorioProducaoResponse(
        Long emPendente,
        Long emCorte,
        Long emCostura,
        Long emAcabamento,
        Long concluidos
) {}
