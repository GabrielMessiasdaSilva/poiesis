package com.poiesis.pedido.repository;

import com.poiesis.pedido.entity.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    List<Pedido> listarPorClienteEmail(String clienteEmail);
}
