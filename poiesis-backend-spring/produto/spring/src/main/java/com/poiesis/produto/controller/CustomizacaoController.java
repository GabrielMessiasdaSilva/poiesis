package com.poiesis.produto.controller;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.service.CustomizacaoService;
import com.poiesis.produto.controller.adapter.CustomizacaoAdapterController;
import com.poiesis.produto.controller.dto.request.CustomizacaoRequest;
import com.poiesis.produto.controller.dto.response.CustomizacaoResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/customizacoes")
public class CustomizacaoController {

    private final CustomizacaoService domainService;

    public CustomizacaoController(CustomizacaoService domainService) {
        this.domainService = domainService;
    }

    @PostMapping
    public ResponseEntity<CustomizacaoResponse> criar(@Valid @RequestBody CustomizacaoRequest dto) {
        OpcaoCustomizacao domain = CustomizacaoAdapterController.toDomain(dto);
        OpcaoCustomizacao criada = domainService.criarOpcao(domain);
        CustomizacaoResponse response = CustomizacaoAdapterController.toResponse(criada);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomizacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CustomizacaoRequest dto) {
        return ResponseEntity.ok(CustomizacaoAdapterController.toResponse(domainService.atualizarOpcao(id, CustomizacaoAdapterController.toDomain(dto))));
    }

    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<List<CustomizacaoResponse>> listarPorProduto(@PathVariable Long produtoId) {
        List<CustomizacaoResponse> lista = domainService.listarPorProduto(produtoId)
                .stream()
                .map(CustomizacaoAdapterController::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return domainService.inativarOpcao(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
