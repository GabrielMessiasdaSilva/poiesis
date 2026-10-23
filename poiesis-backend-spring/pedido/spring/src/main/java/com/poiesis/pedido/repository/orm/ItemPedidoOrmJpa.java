package com.poiesis.pedido.repository.orm;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_item_pedido")
public class ItemPedidoOrmJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;
    private String nomeProduto;
    private Integer quantidade;
    private BigDecimal precoUnitario;
    @Column(length = 300)
    private String textoPersonalizado;
    @Column(length = 2048)
    private String referenciaImagem;

    @ElementCollection
    @CollectionTable(name = "tb_item_customizacao", joinColumns = @JoinColumn(name = "item_id"))
    @OrderColumn(name = "posicao")
    private List<CustomizacaoEscolhidaOrmJpa> customizacoes = new ArrayList<>();

    public List<CustomizacaoEscolhidaOrmJpa> getCustomizacoes() { return customizacoes; }
    public void setCustomizacoes(List<CustomizacaoEscolhidaOrmJpa> opcoes) { customizacoes = new ArrayList<>(opcoes); }

    public ItemPedidoOrmJpa() {}

    public ItemPedidoOrmJpa(Long produtoId, String nomeProduto, Integer quantidade, BigDecimal precoUnitario) {
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
    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }
    public String getReferenciaImagem() { return referenciaImagem; }
    public void setReferenciaImagem(String referenciaImagem) { this.referenciaImagem = referenciaImagem; }
}