package com.poiesis.producao.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Projeção local do contrato JSON publicado pelo serviço de pedidos. */
public record PedidoCriadoEvent(Long pedidoId, String clienteEmail, BigDecimal valorTotal, LocalDateTime dataCriacao) {}
