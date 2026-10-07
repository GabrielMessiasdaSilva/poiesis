package com.poiesis.relatorio.springframework.controller.adapter;

import com.poiesis.relatorio.domain.entity.MetricaVendas;
import com.poiesis.relatorio.domain.entity.RelatorioProducao;
import com.poiesis.relatorio.springframework.controller.dto.response.MetricaVendasResponseDTO;
import com.poiesis.relatorio.springframework.controller.dto.response.RelatorioProducaoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class RelatorioMapper {

    public MetricaVendasResponseDTO toDTO(MetricaVendas domain) {
        if (domain == null) return null;
        return new MetricaVendasResponseDTO(
                domain.totalPedidos(),
                domain.faturamentoTotal(),
                domain.ticketMedio()
        );
    }

    public RelatorioProducaoResponseDTO toDTO(RelatorioProducao domain) {
        if (domain == null) return null;
        return new RelatorioProducaoResponseDTO(
                domain.emPendente(),
                domain.emCorte(),
                domain.emCostura(),
                domain.emAcabamento(),
                domain.concluidos()
        );
    }
}
