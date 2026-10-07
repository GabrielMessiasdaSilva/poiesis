package com.poiesis.customizacao.springframework.controller.adapter;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.springframework.controller.dto.request.CustomizacaoRequestDTO;
import com.poiesis.customizacao.springframework.controller.dto.response.CustomizacaoResponseDTO;
import com.poiesis.customizacao.springframework.repository.entity.CustomizacaoEntity;

public class CustomizacaoMapper {

    // RequestDTO -> Domínio
    public static OpcaoCustomizacao toDomain(CustomizacaoRequestDTO dto) {
        return new OpcaoCustomizacao(
            null, 
            dto.produtoId(),
            dto.tipo(),
            dto.nome(),
            dto.precoAdicional(),
            true
        );
    }

    // Entidade JPA -> Domínio
    public static OpcaoCustomizacao toDomain(CustomizacaoEntity entity) {
        return new OpcaoCustomizacao(
            entity.getId(), 
            entity.getProdutoId(), 
            entity.getTipo(), 
            entity.getNome(), 
            entity.getPrecoAdicional(), 
            entity.getAtivo()
        );
    }

    // Domínio -> Entidade JPA
    public static CustomizacaoEntity toEntity(OpcaoCustomizacao domain) {
        return new CustomizacaoEntity(
            domain.id(),
            domain.produtoId(),
            domain.tipo(),
            domain.nome(),
            domain.precoAdicional(),
            domain.ativo()
        );
    }

    // Domínio -> ResponseDTO
    public static CustomizacaoResponseDTO toResponse(OpcaoCustomizacao domain) {
        return new CustomizacaoResponseDTO(
            domain.id(),
            domain.produtoId(),
            domain.tipo(),
            domain.nome(),
            domain.precoAdicional(),
            domain.ativo()
        );
    }
}
