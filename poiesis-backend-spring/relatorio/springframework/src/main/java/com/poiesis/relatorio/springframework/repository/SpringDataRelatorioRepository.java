package com.poiesis.relatorio.springframework.repository;

import com.poiesis.relatorio.springframework.repository.entity.RelatorioConsolidadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

@Repository
public interface SpringDataRelatorioRepository extends JpaRepository<RelatorioConsolidadoEntity, Long> {

    boolean existsByPedidoId(Long pedidoId);

    @Query("SELECT SUM(r.totalPedidos) FROM RelatorioConsolidadoEntity r WHERE r.dataConsolidacao BETWEEN :inicio AND :fim")
    Long sumTotalPedidosBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(r.faturamentoTotal) FROM RelatorioConsolidadoEntity r WHERE r.dataConsolidacao BETWEEN :inicio AND :fim")
    BigDecimal sumFaturamentoTotalBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
