package com.poiesis.producao.repository.jpa;

import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.repository.orm.OrdemProducaoOrmJpa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdemProducaoRepositoryWithJpa extends JpaRepository<OrdemProducaoOrmJpa, Long> {
    long countByStatus(StatusProducao status);
    Optional<OrdemProducaoOrmJpa> findByPedidoId(Long pedidoId);
}