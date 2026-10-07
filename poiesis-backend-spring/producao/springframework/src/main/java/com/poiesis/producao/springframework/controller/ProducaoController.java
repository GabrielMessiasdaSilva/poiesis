package com.poiesis.producao.springframework.controller;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.domain.service.ProducaoService;
import com.poiesis.producao.springframework.controller.adapter.ProducaoMapper;
import com.poiesis.producao.springframework.controller.dto.request.AtualizarStatusRequestDTO;
import com.poiesis.producao.springframework.controller.dto.response.OrdemProducaoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/producao")
public class ProducaoController {

    private final ProducaoService producaoService;
    private final ProducaoMapper mapper;

    public ProducaoController(ProducaoService producaoService, ProducaoMapper mapper) {
        this.producaoService = producaoService;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrdemProducaoResponseDTO>> listarTodas() {
        List<OrdemProducaoResponseDTO> response = producaoService.listarTodas()
                .stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrdemProducaoResponseDTO> atualizarStatus(
            @PathVariable Long id,
            @RequestBody AtualizarStatusRequestDTO request) {

        OrdemProducao atualizada = producaoService.atualizarStatus(id, request.status());
        return ResponseEntity.ok(mapper.toResponseDTO(atualizada));
    }
}
