package com.poiesis.relatorio.controller.dto.request;

import java.time.LocalDate;

public record FiltroRelatorioVendasRequest(LocalDate dataInicio, LocalDate dataFim) {}
