package com.poiesis.pedido.domain.usecase;

import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;

import java.util.List;
import java.util.Optional;

public class ConsultarPedidosUseCase {
    private final PedidoRepositoryPort pedidoRepository;

    public ConsultarPedidosUseCase(PedidoRepositoryPort pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.listarTodos();
    }

    public List<Pedido> listarPorClienteEmail(String clienteEmail) {
        return pedidoRepository.listarPorClienteEmail(clienteEmail);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.buscarPorId(id);
    }
}
