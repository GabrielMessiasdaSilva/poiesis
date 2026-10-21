package com.poiesis.relatorio.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroVenda(Long pedidoId, LocalDate dataPedido, BigDecimal valorTotal) {}
