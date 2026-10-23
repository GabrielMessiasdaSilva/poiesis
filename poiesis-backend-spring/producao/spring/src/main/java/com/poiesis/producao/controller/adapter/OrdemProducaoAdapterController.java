package com.poiesis.producao.controller.adapter;

import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.controller.dto.response.OrdemProducaoResponse;
import org.springframework.stereotype.Component;

@Component
public class OrdemProducaoAdapterController {

    public OrdemProducaoResponse toResponse(OrdemProducao domain) {
        if (domain == null) return null;
        return new OrdemProducaoResponse(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao(),
                domain.getHistorico()
        );
    }
}
