package com.poiesis.produto.controller.dto.response;

import java.math.BigDecimal;

public record CustomizacaoResponse(
        Long id,
        Long produtoId,
        String tipo,
        String nome,
        BigDecimal precoAdicional,
        Boolean ativo
) {
}
