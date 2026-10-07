package com.poiesis.pedido.springframework.messaging;

import java.math.BigDecimal;

/** Contrato JSON publicado para os consumidores de eventos de pedido. */
public record PedidoCriadoEvent(Long pedidoId, String clienteEmail, BigDecimal valorTotal, java.time.LocalDateTime dataCriacao) {}
