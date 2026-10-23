package com.poiesis.produto.controller.adapter;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.controller.dto.request.CustomizacaoRequest;
import com.poiesis.produto.controller.dto.response.CustomizacaoResponse;

public class CustomizacaoAdapterController {

    public static OpcaoCustomizacao toDomain(CustomizacaoRequest dto) {
        return new OpcaoCustomizacao(
            null, 
            dto.produtoId(),
            dto.tipo(),
            dto.nome(),
            dto.precoAdicional(),
            true
        );
    }

    public static CustomizacaoResponse toResponse(OpcaoCustomizacao domain) {
        return new CustomizacaoResponse(
            domain.id(),
            domain.produtoId(),
            domain.tipo(),
            domain.nome(),
            domain.precoAdicional(),
            domain.ativo()
        );
    }
}
