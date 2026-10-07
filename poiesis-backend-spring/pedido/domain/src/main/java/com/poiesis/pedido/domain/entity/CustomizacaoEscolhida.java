package com.poiesis.pedido.domain.entity;

import java.math.BigDecimal;

/** Cópia da opção e do preço aceitos no momento da compra. */
public record CustomizacaoEscolhida(Long id, String tipo, String nome, BigDecimal precoAdicional) {}
