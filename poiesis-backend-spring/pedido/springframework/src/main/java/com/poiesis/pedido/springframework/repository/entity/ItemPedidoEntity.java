package com.poiesis.pedido.springframework.repository.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_item_pedido")
public class ItemPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;

    @ElementCollection
    @CollectionTable(name = "tb_item_customizacao", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "posicao")
    private java.util.List<CustomizacaoEscolhidaEntity> customizacoes = new java.util.ArrayList<>();

    public java.util.List<CustomizacaoEscolhidaEntity> getCustomizacoes() { return customizacoes; }
    public void setCustomizacoes(java.util.List<CustomizacaoEscolhidaEntity> opcoes) { customizacoes = new java.util.ArrayList<>(opcoes); }

    public ItemPedidoEntity() {}

    public ItemPedidoEntity(Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
}