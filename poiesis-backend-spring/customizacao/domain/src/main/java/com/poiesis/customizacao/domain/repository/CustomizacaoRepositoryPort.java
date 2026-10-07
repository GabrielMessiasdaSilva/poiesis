package com.poiesis.customizacao.domain.repository;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import java.util.List;
import java.util.Optional;

public interface CustomizacaoRepositoryPort {
    OpcaoCustomizacao salvar(OpcaoCustomizacao customizacao);
    Optional<OpcaoCustomizacao> buscarPorId(Long id);
    List<OpcaoCustomizacao> buscarPorProdutoId(Long produtoId);
    boolean inativar(Long id);
}
