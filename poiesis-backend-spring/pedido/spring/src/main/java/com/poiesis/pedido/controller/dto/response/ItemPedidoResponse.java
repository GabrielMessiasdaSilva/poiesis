package com.poiesis.pedido.controller.dto.response;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import java.math.BigDecimal;
import java.util.List;

public class ItemPedidoResponse {
    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
    private List<CustomizacaoEscolhida> customizacoes = List.of();
    private String textoPersonalizado;
    private String referenciaImagem;

    public List<CustomizacaoEscolhida> getCustomizacoes() { return customizacoes; }
    public void setCustomizacoes(List<CustomizacaoEscolhida> opcoes) { customizacoes = opcoes; }
    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }
    public String getReferenciaImagem() { return referenciaImagem; }
    public void setReferenciaImagem(String referenciaImagem) { this.referenciaImagem = referenciaImagem; }

    public ItemPedidoResponse() {}

    public ItemPedidoResponse(Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = subtotal;
    }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}