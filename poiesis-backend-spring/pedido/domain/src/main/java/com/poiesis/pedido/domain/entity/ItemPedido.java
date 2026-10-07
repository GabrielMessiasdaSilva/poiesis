package com.poiesis.pedido.domain.entity;

import java.math.BigDecimal;

public class ItemPedido {

    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private java.util.List<Long> customizacaoIds = java.util.List.of();
    private java.util.List<CustomizacaoEscolhida> customizacoes = java.util.List.of();

    public java.util.List<Long> getCustomizacaoIds() { return customizacaoIds; }
    public void setCustomizacaoIds(java.util.List<Long> ids) { customizacaoIds = ids == null ? java.util.List.of() : ids; }
    public java.util.List<CustomizacaoEscolhida> getCustomizacoes() { return customizacoes; }
    public void setCustomizacoes(java.util.List<CustomizacaoEscolhida> opcoes) { customizacoes = java.util.List.copyOf(opcoes); }

    public ItemPedido() {}

    public ItemPedido(Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getSubtotal() {
        if (precoUnitario == null || quantidade == null) {
            throw new IllegalStateException("Preço e quantidade do item devem estar preenchidos.");
        }
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
}
