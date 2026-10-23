package com.poiesis.produto.controller.adapter;

import com.poiesis.produto.entity.Produto;
import com.poiesis.produto.controller.dto.request.ProdutoRequest;
import com.poiesis.produto.controller.dto.response.ProdutoResponse;
import org.springframework.stereotype.Component;

@Component
public class ProdutoAdapterController {

    public Produto toDomain(ProdutoRequest dto) {
        if (dto == null) return null;
        return new Produto(
                dto.getNome(),
                dto.getDescricao(),
                dto.getPrecoBase(),
                dto.getCategoria(),
                dto.getImagemUrl()
        );
    }

    public ProdutoResponse toResponse(Produto domain) {
        if (domain == null) return null;
        return new ProdutoResponse(
                domain.getId(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getPrecoBase(),
                domain.getCategoria(),
                domain.getAtivo(),
                domain.getImagemUrl()
        );
    }
}
