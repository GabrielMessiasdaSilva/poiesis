package com.poiesis.relatorio.springframework.controller.dto.request;

import java.time.LocalDate;

public record FiltroRelatorioVendasRequestDTO(LocalDate dataInicio, LocalDate dataFim) {}
