package com.poiesis.pedido.domain.port;

import java.math.BigDecimal;

public interface CatalogoServicePort {
    Produto buscarProduto(Long produtoId);

    record Produto(Long id, String nome, BigDecimal preco) {}
}
