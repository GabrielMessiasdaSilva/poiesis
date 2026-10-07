package com.poiesis.pedido.domain.service;

import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;

import java.util.List;

public class PedidoDomainService {

    private final PedidoRepositoryPort pedidoRepositoryPort;

    public PedidoDomainService(PedidoRepositoryPort pedidoRepositoryPort) {
        this.pedidoRepositoryPort = pedidoRepositoryPort;
    }

    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        }
        pedido.calcularValorTotal();
        return pedidoRepositoryPort.salvar(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepositoryPort.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com ID: " + id));
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepositoryPort.listarTodos();
    }
}