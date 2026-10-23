package com.poiesis.produto.repository;

import com.poiesis.produto.entity.Categoria;
import com.poiesis.produto.entity.Produto;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {
    Produto salvar(Produto produto);
    Optional<Produto> buscarPorId(Long id);
    List<Produto> listarTodosAtivos();
    List<Produto> listarPorCategoria(Categoria categoria);
}