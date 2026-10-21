package com.poiesis.producao.service;

import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.messaging.ProducaoPublisher;
import com.poiesis.producao.repository.OrdemProducaoRepository;
import java.util.List;

public class ProducaoService {

    private final OrdemProducaoRepository repository;
    private final ProducaoPublisher publisher;

    public ProducaoService(OrdemProducaoRepository repository, ProducaoPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public OrdemProducao iniciarProducao(Long pedidoId, String clienteEmail) {
        if (pedidoId == null || pedidoId <= 0) {
            throw new IllegalArgumentException("Identificador do pedido deve ser positivo.");
        }
        if (clienteEmail == null || clienteEmail.isBlank()) {
            throw new IllegalArgumentException("E-mail do cliente é obrigatório.");
        }
        // Reutiliza a ordem quando o mesmo pedido é recebido novamente.
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
        if (!ordem.getStatus().podeTransicionarPara(novoStatus)) {
            throw new IllegalArgumentException("Transição inválida: " + ordem.getStatus() + " para " + novoStatus + ".");
        }
        ordem.atualizarStatus(novoStatus);
        OrdemProducao salva = repository.salvar(ordem);
        publisher.notificarAtualizacao(salva);
        return salva;
    }

    public List<OrdemProducao> listarTodas() {
        return repository.listarTodas();
    }

    public OrdemProducao buscarParaCliente(Long pedidoId, String email, boolean administrador) {
        OrdemProducao ordem = repository.buscarPorPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Produção ainda não iniciada para o pedido " + pedidoId + "."));
        if (!administrador && !ordem.getClienteEmail().equalsIgnoreCase(email)) {
            throw new SecurityException("Você não pode consultar a produção deste pedido.");
        }
        return ordem;
    }
}
