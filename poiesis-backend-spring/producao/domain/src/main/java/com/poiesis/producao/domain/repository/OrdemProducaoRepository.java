package com.poiesis.producao.domain.repository;

import com.poiesis.producao.domain.entity.OrdemProducao;
import java.util.Optional;
import java.util.List;

public interface OrdemProducaoRepository {
    OrdemProducao salvar(OrdemProducao ordemProducao);
    Optional<OrdemProducao> buscarPorId(Long id);
    Optional<OrdemProducao> buscarPorPedidoId(Long pedidoId);
    List<OrdemProducao> listarTodas();
}