package com.poiesis.producao.domain.service;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.domain.entity.StatusProducao;
import com.poiesis.producao.domain.repository.OrdemProducaoRepository;

import java.util.List;

public class ProducaoService {

    private final OrdemProducaoRepository repository;

    public ProducaoService(OrdemProducaoRepository repository) {
        this.repository = repository;
    }

    public OrdemProducao iniciarProducao(Long pedidoId, String clienteEmail) {
        OrdemProducao existente = repository.buscarPorPedidoId(pedidoId).orElse(null);
        if (existente != null) {
            return existente;
        }
        OrdemProducao novaOrdem = new OrdemProducao(pedidoId, clienteEmail);
        return repository.salvar(novaOrdem);
    }

    public OrdemProducao atualizarStatus(Long ordemId, StatusProducao novoStatus) {
        OrdemProducao ordem = repository.buscarPorId(ordemId)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de Produção não encontrada ID: " + ordemId));

        ordem.atualizarStatus(novoStatus);
        return repository.salvar(ordem);
    }

    public List<OrdemProducao> listarTodas() {
        return repository.listarTodas();
    }
}
