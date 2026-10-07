package com.poiesis.customizacao.springframework.controller.dto.response;

import java.math.BigDecimal;

public record CustomizacaoResponseDTO(
        Long id,
        Long produtoId,
        String tipo,
        String nome,
        BigDecimal precoAdicional,
        Boolean ativo
) {
}
