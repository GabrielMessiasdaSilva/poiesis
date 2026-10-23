package com.poiesis.producao.controller;

import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.service.ProducaoService;
import com.poiesis.producao.service.ResumoProducaoService;
import com.poiesis.producao.controller.adapter.OrdemProducaoAdapterController;
import com.poiesis.producao.controller.dto.request.AtualizarStatusRequest;
import com.poiesis.producao.controller.dto.response.OrdemProducaoResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/producao")
public class ProducaoController {

    private final ProducaoService producaoService;
    private final OrdemProducaoAdapterController mapper;
    private final ResumoProducaoService consultarResumo;

    public ProducaoController(ProducaoService producaoService, OrdemProducaoAdapterController mapper, ResumoProducaoService consultarResumo) {
        this.producaoService = producaoService;
        this.mapper = mapper;
        this.consultarResumo = consultarResumo;
    }

    @GetMapping("/resumo")
    public Map<String, Long> resumo() {
        var resumo = consultarResumo.consultar();
        return Map.of("emPendente", resumo.emPendente(), "emCorte", resumo.emCorte(),
                "emCostura", resumo.emCostura(), "emAcabamento", resumo.emAcabamento(), "concluidos", resumo.concluidos());
    }

    @GetMapping
    public ResponseEntity<List<OrdemProducaoResponse>> listarTodas() {
        List<OrdemProducaoResponse> response = producaoService.listarTodas()
                .stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pedidos/{pedidoId}")
    public ResponseEntity<OrdemProducaoResponse> consultarPedido(@PathVariable Long pedidoId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Roles") List<String> roles) {
        try {
            return ResponseEntity.ok(mapper.toResponse(producaoService.buscarParaCliente(pedidoId, email, roles.contains("ADMIN"))));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemProducaoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizarStatusRequest request) {

        OrdemProducao atualizada = producaoService.atualizarStatus(id, request.status());
        return ResponseEntity.ok(mapper.toResponse(atualizada));
    }
}
