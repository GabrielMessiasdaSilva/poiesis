package com.poiesis.catalogo.springframework.controller.adapter;

import com.poiesis.catalogo.domain.entity.Produto;
import com.poiesis.catalogo.springframework.controller.dto.request.ProdutoRequestDTO;
import com.poiesis.catalogo.springframework.controller.dto.response.ProdutoResponseDTO;
import com.poiesis.catalogo.springframework.repository.entity.ProdutoEntity;
import org.springframework.stereotype.Component;

@Component
public class CatalogoMapper {

    public Produto toDomain(ProdutoRequestDTO dto) {
        if (dto == null) return null;
        return new Produto(
                dto.getNome(),
                dto.getDescricao(),
                dto.getPrecoBase(),
                dto.getCategoria()
        );
    }

    public ProdutoResponseDTO toDTO(Produto domain) {
        if (domain == null) return null;
        return new ProdutoResponseDTO(
                domain.getId(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getPrecoBase(),
                domain.getCategoria(),
                domain.getAtivo()
        );
    }

    public ProdutoEntity toEntity(Produto domain) {
        if (domain == null) return null;
        return new ProdutoEntity(
                domain.getId(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getPrecoBase(),
                domain.getCategoria(),
                domain.getAtivo()
        );
    }

    public Produto toDomain(ProdutoEntity entity) {
        if (entity == null) return null;
        return new Produto(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPrecoBase(),
                entity.getCategoria(),
                entity.getAtivo()
        );
    }
}