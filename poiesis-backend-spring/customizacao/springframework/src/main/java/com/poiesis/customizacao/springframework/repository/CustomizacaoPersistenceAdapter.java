package com.poiesis.customizacao.springframework.repository;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.domain.repository.CustomizacaoRepositoryPort;
import com.poiesis.customizacao.springframework.controller.adapter.CustomizacaoMapper;
import com.poiesis.customizacao.springframework.repository.entity.CustomizacaoEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CustomizacaoPersistenceAdapter implements CustomizacaoRepositoryPort {

    private final SpringDataCustomizacaoRepository repository;

    public CustomizacaoPersistenceAdapter(SpringDataCustomizacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public OpcaoCustomizacao salvar(OpcaoCustomizacao customizacao) {
        CustomizacaoEntity entity = CustomizacaoMapper.toEntity(customizacao);
        CustomizacaoEntity saved = repository.save(entity);
        return CustomizacaoMapper.toDomain(saved);
    }

    @Override
    public Optional<OpcaoCustomizacao> buscarPorId(Long id) {
        return repository.findById(id).map(CustomizacaoMapper::toDomain);
    }

    @Override
    public List<OpcaoCustomizacao> buscarPorProdutoId(Long produtoId) {
        return repository.findByProdutoIdAndAtivoTrue(produtoId)
                .stream()
                .map(CustomizacaoMapper::toDomain)
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
