package com.poiesis.pedido.integration;

import java.math.BigDecimal;

public interface CatalogoIntegration {
    Produto buscarProduto(Long produtoId);

    record Produto(Long id, String nome, BigDecimal preco) {}
}
