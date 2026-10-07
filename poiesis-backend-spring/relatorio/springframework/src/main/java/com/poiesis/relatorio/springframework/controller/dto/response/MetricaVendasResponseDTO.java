package com.poiesis.relatorio.springframework.controller.dto.response;

import java.math.BigDecimal;

public record MetricaVendasResponseDTO(
        Long totalPedidos,
        BigDecimal faturamentoTotal,
        BigDecimal ticketMedio
) {}
