package com.poiesis.produto.repository;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import java.util.List;
import java.util.Optional;

public interface CustomizacaoRepository {
    OpcaoCustomizacao salvar(OpcaoCustomizacao customizacao);
    Optional<OpcaoCustomizacao> buscarPorId(Long id);
    List<OpcaoCustomizacao> buscarPorProdutoId(Long produtoId);
    boolean inativar(Long id);
}
