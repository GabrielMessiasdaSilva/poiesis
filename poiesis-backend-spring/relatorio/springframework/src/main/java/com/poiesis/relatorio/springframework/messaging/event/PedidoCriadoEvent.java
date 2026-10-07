package com.poiesis.relatorio.springframework.messaging.event;

import java.math.BigDecimal;

/** Projeção local do contrato JSON publicado pelo serviço de pedidos. */
public record PedidoCriadoEvent(Long pedidoId, String clienteEmail, BigDecimal valorTotal) {}
