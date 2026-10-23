package com.poiesis.produto.repository.adapter;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.repository.orm.CustomizacaoOrmJpa;

public class CustomizacaoRepositoryAdapter {

    public static OpcaoCustomizacao toDomain(CustomizacaoOrmJpa entity) {
        return new OpcaoCustomizacao(
            entity.getId(), 
            entity.getProdutoId(), 
            entity.getTipo(), 
            entity.getNome(), 
            entity.getPrecoAdicional(), 
            entity.getAtivo()
        );
    }

    public static CustomizacaoOrmJpa toOrm(OpcaoCustomizacao domain) {
        return new CustomizacaoOrmJpa(
            domain.id(),
            domain.produtoId(),
            domain.tipo(),
            domain.nome(),
            domain.precoAdicional(),
            domain.ativo()
        );
    }
}
