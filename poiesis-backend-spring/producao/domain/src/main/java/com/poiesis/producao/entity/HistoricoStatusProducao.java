package com.poiesis.producao.entity;

import java.time.LocalDateTime;

public record HistoricoStatusProducao(StatusProducao status, LocalDateTime dataHora) {
}