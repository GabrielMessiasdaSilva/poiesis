package com.poiesis.relatorio.controller.dto.response;

import java.math.BigDecimal;

public record MetricaVendasResponse(
        Long totalPedidos,
        BigDecimal faturamentoTotal,
        BigDecimal ticketMedio
) {}
