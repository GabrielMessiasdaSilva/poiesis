package com.poiesis.catalogo.springframework.repository;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.domain.entity.Produto;
import com.poiesis.catalogo.domain.repository.ProdutoRepository;
import com.poiesis.catalogo.springframework.controller.adapter.CatalogoMapper;
import com.poiesis.catalogo.springframework.repository.entity.ProdutoEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ProdutoPersistenceAdapter implements ProdutoRepository {

    private final SpringDataProdutoRepository repository;
    private final CatalogoMapper mapper;

    public ProdutoPersistenceAdapter(SpringDataProdutoRepository repository, CatalogoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Produto salvar(Produto produto) {
        ProdutoEntity entity;
        if (produto.getId() == null) {
            entity = mapper.toEntity(produto);
        } else {
            entity = repository.findById(produto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ID: " + produto.getId()));
            entity.setNome(produto.getNome());
            entity.setDescricao(produto.getDescricao());
            entity.setPrecoBase(produto.getPrecoBase());
            entity.setCategoria(produto.getCategoria());
            entity.setAtivo(produto.getAtivo());
        }
        ProdutoEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Produto> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Produto> listarTodosAtivos() {
        return repository.findByAtivoTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Produto> listarPorCategoria(Categoria categoria) {
        return repository.findByCategoriaAndAtivoTrue(categoria).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
