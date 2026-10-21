package com.poiesis.pedido.repository.jpa;

import com.poiesis.pedido.repository.orm.PedidoOrmJpa;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepositoryWithJpa extends JpaRepository<PedidoOrmJpa, Long> {
    List<PedidoOrmJpa> findByClienteEmailOrderByDataCriacaoDesc(String clienteEmail);
    List<PedidoOrmJpa> findAllByOrderByDataCriacaoDesc();
}
