package com.poiesis.produto.controller.dto.response;

import com.poiesis.produto.entity.Categoria;
import java.math.BigDecimal;

public class ProdutoResponse {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Categoria categoria;
    private Boolean ativo;
    private String imagemUrl;

    public ProdutoResponse() {}

    public ProdutoResponse(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo) {
        this(id, nome, descricao, precoBase, categoria, ativo, null);
    }

    public ProdutoResponse(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo, String imagemUrl) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.categoria = categoria;
        this.ativo = ativo;
        this.imagemUrl = imagemUrl;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public Categoria getCategoria() { return categoria; }
    public Boolean getAtivo() { return ativo; }
    public String getImagemUrl() { return imagemUrl; }
}