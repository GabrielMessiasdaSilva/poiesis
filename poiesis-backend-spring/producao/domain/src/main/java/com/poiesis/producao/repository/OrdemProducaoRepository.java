package com.poiesis.producao.repository;

import com.poiesis.producao.entity.OrdemProducao;
import java.util.List;
import java.util.Optional;

public interface OrdemProducaoRepository {
    OrdemProducao salvar(OrdemProducao ordemProducao);
    Optional<OrdemProducao> buscarPorId(Long id);
    Optional<OrdemProducao> buscarPorPedidoId(Long pedidoId);
    List<OrdemProducao> listarTodas();
}