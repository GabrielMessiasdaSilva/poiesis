package com.poiesis.pedido.messaging;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Contrato JSON publicado para os consumidores de eventos de pedido. */
public record PedidoCriadoEvent(Long pedidoId, String clienteEmail, BigDecimal valorTotal, LocalDateTime dataCriacao) {}
