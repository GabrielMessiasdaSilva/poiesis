package com.poiesis.produto.service;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.service.OpcaoCustomizacaoService;
import java.util.List;

public class CustomizacaoService {
    private final OpcaoCustomizacaoService customizacoes;
    private final CatalogoService produtos;

    public CustomizacaoService(OpcaoCustomizacaoService customizacoes, CatalogoService produtos) {
        this.customizacoes = customizacoes;
        this.produtos = produtos;
    }

    public OpcaoCustomizacao criarOpcao(OpcaoCustomizacao opcao) {
        produtos.buscarPorId(opcao.produtoId());
        return customizacoes.criarOpcao(opcao);
    }

    public OpcaoCustomizacao atualizarOpcao(Long id, OpcaoCustomizacao opcao) {
        produtos.buscarPorId(opcao.produtoId());
        return customizacoes.atualizarOpcao(id, opcao);
    }

    public List<OpcaoCustomizacao> listarPorProduto(Long produtoId) { return customizacoes.listarPorProduto(produtoId); }

    public boolean inativarOpcao(Long id) { return customizacoes.inativarOpcao(id); }
}
