package com.poiesis.pedido.messaging;

import java.time.LocalDateTime;

// Contrato local do evento publicado pelo serviço de produção.
public record ProducaoAtualizadaEvent(Long pedidoId, String clienteEmail, String status,
        LocalDateTime dataAtualizacao) {}
