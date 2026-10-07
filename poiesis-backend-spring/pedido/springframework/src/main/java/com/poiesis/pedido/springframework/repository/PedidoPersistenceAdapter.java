package com.poiesis.pedido.springframework.repository;

import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;
import com.poiesis.pedido.springframework.controller.adapter.PedidoMapper;
import com.poiesis.pedido.springframework.repository.entity.PedidoEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class PedidoPersistenceAdapter implements PedidoRepositoryPort {

    private final SpringDataPedidoRepository springDataPedidoRepository;
    private final PedidoMapper pedidoMapper;

    public PedidoPersistenceAdapter(SpringDataPedidoRepository springDataPedidoRepository, PedidoMapper pedidoMapper) {
        this.springDataPedidoRepository = springDataPedidoRepository;
        this.pedidoMapper = pedidoMapper;
    }

    @Override
    @Transactional
    public Pedido salvar(Pedido pedido) {
        PedidoEntity entity = pedidoMapper.toEntity(pedido);
        PedidoEntity savedEntity = springDataPedidoRepository.save(entity);
        return pedidoMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(Long id) {
        return springDataPedidoRepository.findById(id)
                .map(pedidoMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return springDataPedidoRepository.findAllByOrderByDataCriacaoDesc()
                .stream().map(pedidoMapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorClienteEmail(String clienteEmail) {
        return springDataPedidoRepository.findByClienteEmailOrderByDataCriacaoDesc(clienteEmail)
                .stream().map(pedidoMapper::toDomain).toList();
    }
}
