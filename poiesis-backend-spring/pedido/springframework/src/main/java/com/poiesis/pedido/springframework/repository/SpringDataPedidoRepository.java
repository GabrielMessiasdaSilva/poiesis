package com.poiesis.pedido.springframework.repository;

import com.poiesis.pedido.springframework.repository.entity.PedidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPedidoRepository extends JpaRepository<PedidoEntity, Long> {
    java.util.List<PedidoEntity> findByClienteEmailOrderByDataCriacaoDesc(String clienteEmail);
    java.util.List<PedidoEntity> findAllByOrderByDataCriacaoDesc();
}
