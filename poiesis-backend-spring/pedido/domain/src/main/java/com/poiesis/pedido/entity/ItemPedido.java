package com.poiesis.pedido.entity;

import java.math.BigDecimal;
import java.util.List;

public class ItemPedido {

    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private List<Long> customizacaoIds = List.of();
    private List<CustomizacaoEscolhida> customizacoes = List.of();
    private String textoPersonalizado;
    private String referenciaImagem;

    public List<Long> getCustomizacaoIds() { return customizacaoIds; }
    public void setCustomizacaoIds(List<Long> ids) { customizacaoIds = ids == null ? List.of() : ids; }
    public List<CustomizacaoEscolhida> getCustomizacoes() { return customizacoes; }
    public void setCustomizacoes(List<CustomizacaoEscolhida> opcoes) { customizacoes = List.copyOf(opcoes); }
    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }
    public String getReferenciaImagem() { return referenciaImagem; }
    public void setReferenciaImagem(String referenciaImagem) { this.referenciaImagem = referenciaImagem; }

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
