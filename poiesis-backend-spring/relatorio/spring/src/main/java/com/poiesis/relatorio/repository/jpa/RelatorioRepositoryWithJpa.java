package com.poiesis.relatorio.repository.jpa;

import com.poiesis.relatorio.repository.orm.RelatorioConsolidadoOrmJpa;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioRepositoryWithJpa extends JpaRepository<RelatorioConsolidadoOrmJpa, Long> {

    boolean existsByPedidoId(Long pedidoId);

    @Query("SELECT SUM(r.totalPedidos) FROM RelatorioConsolidadoOrmJpa r WHERE r.dataConsolidacao BETWEEN :inicio AND :fim")
    Long sumTotalPedidosBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT SUM(r.faturamentoTotal) FROM RelatorioConsolidadoOrmJpa r WHERE r.dataConsolidacao BETWEEN :inicio AND :fim")
    BigDecimal sumFaturamentoTotalBetween(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
