package com.poiesis.relatorio.springframework.controller;

import com.poiesis.relatorio.domain.service.RelatorioService;
import com.poiesis.relatorio.springframework.controller.adapter.RelatorioMapper;
import com.poiesis.relatorio.springframework.controller.dto.response.MetricaVendasResponseDTO;
import com.poiesis.relatorio.springframework.controller.dto.response.RelatorioProducaoResponseDTO;
import com.poiesis.relatorio.springframework.controller.dto.request.FiltroRelatorioVendasRequestDTO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;
    private final RelatorioMapper relatorioMapper;

    public RelatorioController(RelatorioService relatorioService, RelatorioMapper relatorioMapper) {
        this.relatorioService = relatorioService;
        this.relatorioMapper = relatorioMapper;
    }

    @GetMapping("/vendas")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricaVendasResponseDTO> obterRelatorioVendas(
            @RequestParam("dataInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam("dataFim") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        var metricas = relatorioService.gerarRelatorioVendas(dataInicio, dataFim);
        return ResponseEntity.ok(relatorioMapper.toDTO(metricas));
    }

    @PostMapping("/vendas/filtrar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricaVendasResponseDTO> filtrarRelatorioVendas(
            @RequestBody FiltroRelatorioVendasRequestDTO request) {

        var metricas = relatorioService.gerarRelatorioVendas(request.dataInicio(), request.dataFim());
        return ResponseEntity.ok(relatorioMapper.toDTO(metricas));
    }

    @GetMapping("/producao")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RelatorioProducaoResponseDTO> obterRelatorioProducao() {
        var relatorio = relatorioService.gerarRelatorioProducao();
        return ResponseEntity.ok(relatorioMapper.toDTO(relatorio));
    }
}
