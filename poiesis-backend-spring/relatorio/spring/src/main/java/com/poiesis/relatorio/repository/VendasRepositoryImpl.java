package com.poiesis.relatorio.repository;

import com.poiesis.relatorio.entity.MetricaVendas;
import com.poiesis.relatorio.entity.RegistroVenda;
import com.poiesis.relatorio.repository.VendasRepository;
import com.poiesis.relatorio.repository.jpa.RelatorioRepositoryWithJpa;
import com.poiesis.relatorio.repository.orm.RelatorioConsolidadoOrmJpa;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class VendasRepositoryImpl implements VendasRepository {

    private final RelatorioRepositoryWithJpa repository;

    public VendasRepositoryImpl(RelatorioRepositoryWithJpa repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public MetricaVendas obterMetricasVendas(LocalDate dataInicio, LocalDate dataFim) {
        Long totalPedidos = repository.sumTotalPedidosBetween(dataInicio, dataFim);
        BigDecimal faturamentoTotal = repository.sumFaturamentoTotalBetween(dataInicio, dataFim);

        return new MetricaVendas(
                totalPedidos != null ? totalPedidos : 0L,
                faturamentoTotal != null ? faturamentoTotal : BigDecimal.ZERO
        );
    }

    @Override
    public boolean existePorPedidoId(Long pedidoId) { return repository.existsByPedidoId(pedidoId); }

    @Override
    @Transactional
    public void registrar(RegistroVenda venda) {
        if (repository.existsByPedidoId(venda.pedidoId())) return;
        repository.save(new RelatorioConsolidadoOrmJpa(
                venda.dataPedido(), 1L, venda.valorTotal(), venda.pedidoId()));
    }
}
