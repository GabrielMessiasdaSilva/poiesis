package com.poiesis.catalogo.domain.service;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.domain.entity.Produto;
import com.poiesis.catalogo.domain.repository.ProdutoRepository;

import java.util.List;

public class CatalogoService {

    private final ProdutoRepository produtoRepository;

    public CatalogoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto cadastrarProduto(Produto produto) {
        // Um produto precisa de nome, categoria e preço positivo para entrar no catálogo.
        if (produto.getNome() == null || produto.getNome().isBlank() || produto.getCategoria() == null) {
            throw new IllegalArgumentException("Nome e categoria são obrigatórios.");
        }
        if (produto.getPrecoBase() == null || produto.getPrecoBase().doubleValue() <= 0) {
            throw new IllegalArgumentException("O preço base do produto deve ser maior que zero.");
        }
        return produtoRepository.salvar(produto);
    }

    public Produto atualizarProduto(Long id, Produto dados) {
        Produto atual = buscarPorId(id);
        return cadastrarProduto(new Produto(id, dados.getNome(), dados.getDescricao(),
                dados.getPrecoBase(), dados.getCategoria(), atual.getAtivo()));
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ID: " + id));
    }

    public List<Produto> listarProdutosAtivos() {
        return produtoRepository.listarTodosAtivos();
    }

    public List<Produto> listarPorCategoria(Categoria categoria) {
        return produtoRepository.listarPorCategoria(categoria);
    }

    public void inativarProduto(Long id) {
        Produto produto = buscarPorId(id);
        // Inativa sem excluir o registro, preservando os dados do produto.
        produto.desativar();
        produtoRepository.salvar(produto);
    }
}
