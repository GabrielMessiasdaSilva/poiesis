package com.poiesis.producao.repository.adapter;

import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.entity.HistoricoStatusProducao;
import com.poiesis.producao.repository.orm.HistoricoStatusOrmJpa;
import com.poiesis.producao.repository.orm.OrdemProducaoOrmJpa;
import org.springframework.stereotype.Component;

@Component
public class OrdemProducaoRepositoryAdapter {

    public OrdemProducaoOrmJpa toOrm(OrdemProducao domain) {
        if (domain == null) return null;
        var orm = new OrdemProducaoOrmJpa(
                domain.getId(),
                domain.getPedidoId(),
                domain.getClienteEmail(),
                domain.getStatus(),
                domain.getDataInicio(),
                domain.getDataAtualizacao()
        );
            orm.setHistorico(domain.getHistorico().stream()
                .map(item -> new HistoricoStatusOrmJpa(item.status(), item.dataHora())).toList());
            return orm;
    }

    public OrdemProducao toDomain(OrdemProducaoOrmJpa entity) {
        if (entity == null) return null;
        return new OrdemProducao(
                entity.getId(),
                entity.getPedidoId(),
                entity.getClienteEmail(),
                entity.getStatus(),
                entity.getDataInicio(),
                entity.getDataAtualizacao(),
                entity.getHistorico().stream()
                    .map(item -> new HistoricoStatusProducao(item.getStatus(), item.getDataHora())).toList()
        );
    }
}
