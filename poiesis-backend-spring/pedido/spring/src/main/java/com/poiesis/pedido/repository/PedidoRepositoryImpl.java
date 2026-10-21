package com.poiesis.pedido.repository;

import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.repository.PedidoRepository;
import com.poiesis.pedido.repository.adapter.PedidoRepositoryAdapter;
import com.poiesis.pedido.repository.jpa.PedidoRepositoryWithJpa;
import com.poiesis.pedido.repository.orm.PedidoOrmJpa;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PedidoRepositoryImpl implements PedidoRepository {

    private final PedidoRepositoryWithJpa springDataPedidoRepository;
    private final PedidoRepositoryAdapter pedidoAdapter;

    public PedidoRepositoryImpl(PedidoRepositoryWithJpa springDataPedidoRepository, PedidoRepositoryAdapter pedidoAdapter) {
        this.springDataPedidoRepository = springDataPedidoRepository;
        this.pedidoAdapter = pedidoAdapter;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoOrmJpa entity = pedidoAdapter.toOrm(pedido);
        PedidoOrmJpa savedEntity = springDataPedidoRepository.save(entity);
        return pedidoAdapter.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return springDataPedidoRepository.findById(id)
                .map(pedidoAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return springDataPedidoRepository.findAllByOrderByDataCriacaoDesc()
                .stream().map(pedidoAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorClienteEmail(String clienteEmail) {
        return springDataPedidoRepository.findByClienteEmailOrderByDataCriacaoDesc(clienteEmail)
                .stream().map(pedidoAdapter::toDomain).toList();
    }
}
