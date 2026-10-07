package com.poiesis.producao.springframework.controller.adapter;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.springframework.controller.dto.response.OrdemProducaoResponseDTO;
import com.poiesis.producao.springframework.repository.entity.OrdemProducaoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProducaoMapper {

    public OrdemProducaoResponseDTO toResponseDTO(OrdemProducao domain) {
        if (domain == null) return null;
        return new OrdemProducaoResponseDTO(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao()
        );
    }

    public OrdemProducaoEntity toEntity(OrdemProducao domain) {
        if (domain == null) return null;
        return new OrdemProducaoEntity(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao()
        );
    }

    public OrdemProducao toDomain(OrdemProducaoEntity entity) {
        if (entity == null) return null;
        return new OrdemProducao(
                entity.getId(),
                entity.getPedidoId(),
                entity.getClienteEmail(),
                entity.getStatus(),
                entity.getDataInicio(),
                entity.getDataAtualizacao()
        );
    }
}