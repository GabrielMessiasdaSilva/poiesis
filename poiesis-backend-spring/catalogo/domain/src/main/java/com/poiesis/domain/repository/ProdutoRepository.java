package com.poiesis.catalogo.domain.repository;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.domain.entity.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> listarTodosAtivos();
    List<Produto> listarPorCategoria(Categoria categoria);
}