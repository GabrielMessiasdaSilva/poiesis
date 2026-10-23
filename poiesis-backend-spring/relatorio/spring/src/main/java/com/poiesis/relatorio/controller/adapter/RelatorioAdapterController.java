package com.poiesis.relatorio.controller.adapter;

import com.poiesis.relatorio.entity.MetricaVendas;
import com.poiesis.relatorio.entity.RelatorioProducao;
import com.poiesis.relatorio.controller.dto.response.MetricaVendasResponse;
import com.poiesis.relatorio.controller.dto.response.RelatorioProducaoResponse;
import org.springframework.stereotype.Component;

@Component
public class RelatorioAdapterController {

    public MetricaVendasResponse toResponse(MetricaVendas domain) {
        if (domain == null) return null;
        return new MetricaVendasResponse(
                domain.totalPedidos(),
                domain.faturamentoTotal(),
                domain.ticketMedio()
        );
    }

    public RelatorioProducaoResponse toResponse(RelatorioProducao domain) {
        if (domain == null) return null;
        return new RelatorioProducaoResponse(
                domain.emPendente(),
                domain.emCorte(),
                domain.emCostura(),
                domain.emAcabamento(),
                domain.concluidos()
        );
    }
}
