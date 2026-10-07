package com.poiesis.customizacao.springframework.controller;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.domain.service.CustomizacaoDomainService;
import com.poiesis.customizacao.springframework.controller.adapter.CustomizacaoMapper;
import com.poiesis.customizacao.springframework.controller.dto.request.CustomizacaoRequestDTO;
import com.poiesis.customizacao.springframework.controller.dto.response.CustomizacaoResponseDTO;
import com.poiesis.customizacao.springframework.integration.CatalogoFeignClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/customizacoes")
public class CustomizacaoController {

    private final CustomizacaoDomainService domainService;
    private final CatalogoFeignClient catalogoFeignClient;

    public CustomizacaoController(CustomizacaoDomainService domainService, CatalogoFeignClient catalogoFeignClient) {
        this.domainService = domainService;
        this.catalogoFeignClient = catalogoFeignClient;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public ResponseEntity<CustomizacaoResponseDTO> criar(@Valid @RequestBody CustomizacaoRequestDTO dto) {
        // Valida se o produto existe no microsserviço de Catálogo via Feign Client
        catalogoFeignClient.buscarProdutoPorId(dto.produtoId());

        OpcaoCustomizacao domain = CustomizacaoMapper.toDomain(dto);
        OpcaoCustomizacao criada = domainService.criarOpcao(domain);
        CustomizacaoResponseDTO response = CustomizacaoMapper.toResponse(criada);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/produto/{produtoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CustomizacaoResponseDTO>> listarPorProduto(@PathVariable Long produtoId) {
        List<CustomizacaoResponseDTO> lista = domainService.listarPorProduto(produtoId)
                .stream()
                .map(CustomizacaoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        return domainService.inativarOpcao(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
