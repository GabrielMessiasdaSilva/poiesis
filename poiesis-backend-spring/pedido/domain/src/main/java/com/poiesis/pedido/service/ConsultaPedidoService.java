package com.poiesis.pedido.service;

import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.repository.PedidoRepository;
import java.util.List;
import java.util.Optional;

public class ConsultaPedidoService {
    private final PedidoRepository pedidoRepository;

    public ConsultaPedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<Pedido> listarParaCliente(String email, boolean administrador) {
        return administrador ? pedidoRepository.listarTodos() : pedidoRepository.listarPorClienteEmail(email);
    }

    public Optional<Pedido> buscarParaCliente(Long id, String email, boolean administrador) {
        return pedidoRepository.buscarPorId(id)
                .filter(pedido -> administrador || pedido.getClienteEmail().equals(email));
    }
}
