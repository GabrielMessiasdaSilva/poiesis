package com.poiesis.producao.springframework.repository;

import com.poiesis.producao.springframework.repository.entity.OrdemProducaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataOrdemProducaoRepository extends JpaRepository<OrdemProducaoEntity, Long> {
    Optional<OrdemProducaoEntity> findByPedidoId(Long pedidoId);
}