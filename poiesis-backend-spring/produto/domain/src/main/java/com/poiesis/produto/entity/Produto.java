package com.poiesis.produto.entity;

import java.math.BigDecimal;

public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Categoria categoria;
    private Boolean ativo;
    private String imagemUrl;

    public Produto() {}

    public Produto(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo) {
        this(id, nome, descricao, precoBase, categoria, ativo, null);
    }

    public Produto(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo, String imagemUrl) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.categoria = categoria;
        this.ativo = ativo != null ? ativo : true;
        this.imagemUrl = imagemUrl;
    }

    public Produto(String nome, String descricao, BigDecimal precoBase, Categoria categoria) {
        this(nome, descricao, precoBase, categoria, null);
    }

    public Produto(String nome, String descricao, BigDecimal precoBase, Categoria categoria, String imagemUrl) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.categoria = categoria;
        this.ativo = true;
        this.imagemUrl = imagemUrl;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public Categoria getCategoria() { return categoria; }
    public Boolean getAtivo() { return ativo; }
    public String getImagemUrl() { return imagemUrl; }
}