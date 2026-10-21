package com.poiesis.produto.repository;

import com.poiesis.produto.entity.Categoria;
import com.poiesis.produto.entity.Produto;
import com.poiesis.produto.repository.ProdutoRepository;
import com.poiesis.produto.repository.adapter.ProdutoRepositoryAdapter;
import com.poiesis.produto.repository.jpa.ProdutoRepositoryWithJpa;
import com.poiesis.produto.repository.orm.ProdutoOrmJpa;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ProdutoRepositoryImpl implements ProdutoRepository {

    private final ProdutoRepositoryWithJpa repository;
    private final ProdutoRepositoryAdapter mapper;

    public ProdutoRepositoryImpl(ProdutoRepositoryWithJpa repository, ProdutoRepositoryAdapter mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Produto salvar(Produto produto) {
        ProdutoOrmJpa entity;
        if (produto.getId() == null) {
            entity = mapper.toOrm(produto);
        } else {
            entity = repository.findById(produto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado ID: " + produto.getId()));
            entity.setNome(produto.getNome());
            entity.setDescricao(produto.getDescricao());
            entity.setPrecoBase(produto.getPrecoBase());
            entity.setCategoria(produto.getCategoria());
            entity.setAtivo(produto.getAtivo());
        }
        ProdutoOrmJpa saved = repository.save(entity);
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
