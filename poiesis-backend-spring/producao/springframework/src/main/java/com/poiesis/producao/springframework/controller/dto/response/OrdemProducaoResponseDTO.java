package com.poiesis.producao.springframework.controller.dto.response;

import com.poiesis.producao.domain.entity.StatusProducao;

import java.time.LocalDateTime;

public record OrdemProducaoResponseDTO(
        Long id,
        Long pedidoId,
        String clienteEmail,
        StatusProducao status,
        LocalDateTime dataInicio,
        LocalDateTime dataAtualizacao
) {
}
