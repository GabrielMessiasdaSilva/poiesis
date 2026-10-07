package com.poiesis.customizacao.domain.entity;

import java.math.BigDecimal;

public record OpcaoCustomizacao(
        Long id,
        Long produtoId,
        String tipo,
        String nome,
        BigDecimal precoAdicional,
        Boolean ativo
) {
    public OpcaoCustomizacao {
        precoAdicional = precoAdicional != null ? precoAdicional : BigDecimal.ZERO;
        ativo = ativo != null ? ativo : true;
    }
}
