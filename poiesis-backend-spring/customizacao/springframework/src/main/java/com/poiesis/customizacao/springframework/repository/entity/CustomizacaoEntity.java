package com.poiesis.customizacao.springframework.repository.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_customizacoes")
public class CustomizacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Long produtoId;

    @Column(nullable = false, length = 80)
    private String tipo;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAdicional;
    @Column(nullable = false)
    private Boolean ativo;

    public CustomizacaoEntity() {}

    public CustomizacaoEntity(Long id, Long produtoId, String tipo, String nome, BigDecimal precoAdicional, Boolean ativo) {
        this.id = id;
        this.produtoId = produtoId;
        this.tipo = tipo;
        this.nome = nome;
        this.precoAdicional = precoAdicional;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoAdicional() { return precoAdicional; }
    public void setPrecoAdicional(BigDecimal precoAdicional) { this.precoAdicional = precoAdicional; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
