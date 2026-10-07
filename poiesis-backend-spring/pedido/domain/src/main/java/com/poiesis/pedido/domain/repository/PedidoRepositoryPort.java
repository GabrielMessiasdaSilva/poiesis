package com.poiesis.pedido.domain.repository;

import com.poiesis.pedido.domain.entity.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoRepositoryPort {
    Pedido salvar(Pedido pedido);
    Optional<Pedido> buscarPorId(Long id);
    List<Pedido> listarTodos();
    List<Pedido> listarPorClienteEmail(String clienteEmail);
}
