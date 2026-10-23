package com.poiesis.producao.repository.orm;

import com.poiesis.producao.entity.StatusProducao;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Embeddable
public class HistoricoStatusOrmJpa {
    @Enumerated(EnumType.STRING)
    private StatusProducao status;
    private LocalDateTime dataHora;

    public HistoricoStatusOrmJpa() {}

    public HistoricoStatusOrmJpa(StatusProducao status, LocalDateTime dataHora) {
        this.status = status;
        this.dataHora = dataHora;
    }

    public StatusProducao getStatus() { return status; }
    public LocalDateTime getDataHora() { return dataHora; }
}