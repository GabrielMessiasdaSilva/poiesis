package com.poiesis.catalogo.springframework.controller;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.domain.entity.Produto;
import com.poiesis.catalogo.domain.service.CatalogoService;
import com.poiesis.catalogo.springframework.controller.adapter.CatalogoMapper;
import com.poiesis.catalogo.springframework.controller.dto.request.ProdutoRequestDTO;
import com.poiesis.catalogo.springframework.controller.dto.response.ProdutoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/produtos")
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final CatalogoMapper catalogoMapper;

    public CatalogoController(CatalogoService catalogoService, CatalogoMapper catalogoMapper) {
        this.catalogoService = catalogoService;
        this.catalogoMapper = catalogoMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@RequestBody ProdutoRequestDTO requestDTO) {
        Produto produto = catalogoMapper.toDomain(requestDTO);
        Produto salvo = catalogoService.cadastrarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoMapper.toDTO(salvo));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @RequestBody ProdutoRequestDTO dto) {
        return ResponseEntity.ok(catalogoMapper.toDTO(catalogoService.atualizarProduto(id, catalogoMapper.toDomain(dto))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        Produto produto = catalogoService.buscarPorId(id);
        return ResponseEntity.ok(catalogoMapper.toDTO(produto));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listarAtivos(
            @RequestParam(required = false) Categoria categoria) {
        List<Produto> lista = (categoria != null) ?
                catalogoService.listarPorCategoria(categoria) :
                catalogoService.listarProdutosAtivos();

        List<ProdutoResponseDTO> response = lista.stream()
                .map(catalogoMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        catalogoService.inativarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
