package com.poiesis.produto.repository;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.repository.CustomizacaoRepository;
import com.poiesis.produto.repository.adapter.CustomizacaoRepositoryAdapter;
import com.poiesis.produto.repository.jpa.CustomizacaoRepositoryWithJpa;
import com.poiesis.produto.repository.orm.CustomizacaoOrmJpa;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CustomizacaoRepositoryImpl implements CustomizacaoRepository {

    private final CustomizacaoRepositoryWithJpa repository;

    public CustomizacaoRepositoryImpl(CustomizacaoRepositoryWithJpa repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OpcaoCustomizacao salvar(OpcaoCustomizacao customizacao) {
        CustomizacaoOrmJpa entity;
        if (customizacao.id() == null) {
            entity = CustomizacaoRepositoryAdapter.toOrm(customizacao);
        } else {
            entity = repository.findById(customizacao.id())
                    .orElseThrow(() -> new IllegalArgumentException("Customização não encontrada."));
            entity.setProdutoId(customizacao.produtoId());
            entity.setTipo(customizacao.tipo());
            entity.setNome(customizacao.nome());
            entity.setPrecoAdicional(customizacao.precoAdicional());
            entity.setAtivo(customizacao.ativo());
        }
        CustomizacaoOrmJpa saved = repository.save(entity);
        return CustomizacaoRepositoryAdapter.toDomain(saved);
    }

    @Override
    public Optional<OpcaoCustomizacao> buscarPorId(Long id) {
        return repository.findById(id).map(CustomizacaoRepositoryAdapter::toDomain);
    }

    @Override
    public List<OpcaoCustomizacao> buscarPorProdutoId(Long produtoId) {
        return repository.findByProdutoIdAndAtivoTrue(produtoId)
                .stream()
                .map(CustomizacaoRepositoryAdapter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean inativar(Long id) {
        return repository.findById(id).map(entity -> {
            if (Boolean.TRUE.equals(entity.getAtivo())) {
                entity.setAtivo(false);
                repository.save(entity);
            }
            return true;
        }).orElse(false);
    }
}
