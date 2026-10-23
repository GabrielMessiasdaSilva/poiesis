package com.poiesis.producao.messaging;

import com.poiesis.producao.entity.StatusProducao;
import java.time.LocalDateTime;

public record ProducaoAtualizadaEvent(Long pedidoId, String clienteEmail, StatusProducao status,
        LocalDateTime dataAtualizacao) {
}