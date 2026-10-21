package com.poiesis.produto.repository.adapter;

import com.poiesis.produto.entity.Produto;
import com.poiesis.produto.repository.orm.ProdutoOrmJpa;
import org.springframework.stereotype.Component;

@Component
public class ProdutoRepositoryAdapter {

    public ProdutoOrmJpa toOrm(Produto domain) {
        if (domain == null) return null;
        return new ProdutoOrmJpa(
                domain.getId(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getPrecoBase(),
                domain.getCategoria(),
                domain.getAtivo(),
                domain.getImagemUrl()
        );
    }

    public Produto toDomain(ProdutoOrmJpa entity) {
        if (entity == null) return null;
        return new Produto(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getPrecoBase(),
                entity.getCategoria(),
                entity.getAtivo(),
                entity.getImagemUrl()
        );
    }
}
