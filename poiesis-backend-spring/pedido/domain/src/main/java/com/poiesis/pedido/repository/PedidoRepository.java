package com.poiesis.pedido.repository;

import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.entity.StatusPedido;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    List<Pedido> listarPorClienteEmail(String clienteEmail);
    void atualizarStatus(Long pedidoId, String clienteEmail, StatusPedido status);
}
