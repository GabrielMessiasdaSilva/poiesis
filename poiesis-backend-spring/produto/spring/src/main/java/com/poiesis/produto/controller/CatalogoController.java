package com.poiesis.produto.controller;

import com.poiesis.produto.entity.Categoria;
import com.poiesis.produto.entity.Produto;
import com.poiesis.produto.service.CatalogoService;
import com.poiesis.produto.controller.adapter.ProdutoAdapterController;
import com.poiesis.produto.controller.dto.request.ProdutoRequest;
import com.poiesis.produto.controller.dto.response.ProdutoResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/produtos")
public class CatalogoController {

    private final CatalogoService catalogoService;
    private final ProdutoAdapterController catalogoAdapter;

    public CatalogoController(CatalogoService catalogoService, ProdutoAdapterController catalogoAdapter) {
        this.catalogoService = catalogoService;
        this.catalogoAdapter = catalogoAdapter;
    }

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@RequestBody ProdutoRequest requestDTO) {
        Produto produto = catalogoAdapter.toDomain(requestDTO);
        Produto salvo = catalogoService.cadastrarProduto(produto);
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogoAdapter.toResponse(salvo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable Long id, @RequestBody ProdutoRequest dto) {
        return ResponseEntity.ok(catalogoAdapter.toResponse(catalogoService.atualizarProduto(id, catalogoAdapter.toDomain(dto))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable Long id) {
        Produto produto = catalogoService.buscarPorId(id);
        return ResponseEntity.ok(catalogoAdapter.toResponse(produto));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarAtivos(
            @RequestParam(required = false) Categoria categoria) {
        List<Produto> lista = (categoria != null) ?
                catalogoService.listarPorCategoria(categoria) :
                catalogoService.listarProdutosAtivos();

        List<ProdutoResponse> response = lista.stream()
                .map(catalogoAdapter::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        catalogoService.inativarProduto(id);
        return ResponseEntity.noContent().build();
    }
}
