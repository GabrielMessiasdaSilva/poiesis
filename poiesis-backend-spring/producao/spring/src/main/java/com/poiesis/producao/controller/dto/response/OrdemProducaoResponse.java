package com.poiesis.producao.controller.dto.response;

import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.entity.HistoricoStatusProducao;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemProducaoResponse(
        Long id,
        Long pedidoId,
        String clienteEmail,
        StatusProducao status,
        LocalDateTime dataInicio,
        LocalDateTime dataAtualizacao,
        List<HistoricoStatusProducao> historico
) {
}
