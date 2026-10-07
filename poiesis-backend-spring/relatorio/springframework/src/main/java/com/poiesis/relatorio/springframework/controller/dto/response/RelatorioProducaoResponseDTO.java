package com.poiesis.relatorio.springframework.controller.dto.response;

public record RelatorioProducaoResponseDTO(
        Long emPendente,
        Long emCorte,
        Long emCostura,
        Long emAcabamento,
        Long concluidos
) {}
