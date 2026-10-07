package com.poiesis.relatorio.domain.entity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record MetricaVendas(Long totalPedidos, BigDecimal faturamentoTotal) {

    public MetricaVendas {
        totalPedidos = totalPedidos != null ? totalPedidos : 0L;
        faturamentoTotal = faturamentoTotal != null ? faturamentoTotal : BigDecimal.ZERO;
    }

    public BigDecimal ticketMedio() {
        if (totalPedidos == 0 || faturamentoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return faturamentoTotal.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);
    }
}
