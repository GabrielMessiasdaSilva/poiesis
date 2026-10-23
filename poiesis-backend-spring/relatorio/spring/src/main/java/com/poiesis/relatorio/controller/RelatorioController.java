package com.poiesis.relatorio.controller;

import com.poiesis.relatorio.service.RelatorioService;
import com.poiesis.relatorio.controller.adapter.RelatorioAdapterController;
import com.poiesis.relatorio.controller.dto.request.FiltroRelatorioVendasRequest;
import com.poiesis.relatorio.controller.dto.response.MetricaVendasResponse;
import com.poiesis.relatorio.controller.dto.response.RelatorioProducaoResponse;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final RelatorioAdapterController relatorioMapper;

    public RelatorioController(RelatorioService relatorioService, RelatorioAdapterController relatorioMapper) {
        this.relatorioService = relatorioService;
        this.relatorioMapper = relatorioMapper;
    }

    @GetMapping("/vendas")
    public ResponseEntity<MetricaVendasResponse> obterRelatorioVendas(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        var metricas = relatorioService.gerarRelatorioVendas(dataInicio, dataFim);
        return ResponseEntity.ok(relatorioMapper.toResponse(metricas));
    }

    @PostMapping("/vendas/filtrar")
    public ResponseEntity<MetricaVendasResponse> filtrarRelatorioVendas(
            @RequestBody FiltroRelatorioVendasRequest request) {

        var metricas = relatorioService.gerarRelatorioVendas(request.dataInicio(), request.dataFim());
        return ResponseEntity.ok(relatorioMapper.toResponse(metricas));
    }

    @GetMapping("/producao")
    public ResponseEntity<RelatorioProducaoResponse> obterRelatorioProducao() {
        var relatorio = relatorioService.gerarRelatorioProducao();
        return ResponseEntity.ok(relatorioMapper.toResponse(relatorio));
    }
}
