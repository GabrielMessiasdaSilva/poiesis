package com.poiesis.customizacao.domain.service;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.domain.repository.CustomizacaoRepositoryPort;

import java.math.BigDecimal;
import java.util.List;

public class CustomizacaoDomainService {

    private final CustomizacaoRepositoryPort customizacaoRepositoryPort;

    public CustomizacaoDomainService(CustomizacaoRepositoryPort customizacaoRepositoryPort) {
        this.customizacaoRepositoryPort = customizacaoRepositoryPort;
    }

    public OpcaoCustomizacao criarOpcao(OpcaoCustomizacao customizacao) {
        if (customizacao == null) {
            throw new IllegalArgumentException("A opção de customização é obrigatória.");
        }
        if (customizacao.produtoId() == null || customizacao.produtoId() <= 0) {
            throw new IllegalArgumentException("A opção de customização precisa estar associada a um produto.");
        }
        if (customizacao.tipo() == null || customizacao.tipo().isBlank()) {
            throw new IllegalArgumentException("O tipo da customização é obrigatório.");
        }
        if (customizacao.nome() == null || customizacao.nome().isBlank()) {
            throw new IllegalArgumentException("O nome da customização é obrigatório.");
        }
        if (customizacao.precoAdicional() == null || customizacao.precoAdicional().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O preço adicional não pode ser negativo.");
        }
        return customizacaoRepositoryPort.salvar(customizacao);
    }

    public List<OpcaoCustomizacao> listarPorProduto(Long produtoId) {
        if (produtoId == null || produtoId <= 0) {
            throw new IllegalArgumentException("O ID do produto deve ser positivo.");
        }
        return customizacaoRepositoryPort.buscarPorProdutoId(produtoId);
    }

    public boolean inativarOpcao(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("O ID da customização deve ser positivo.");
        }
        return customizacaoRepositoryPort.inativar(id);
    }
}
