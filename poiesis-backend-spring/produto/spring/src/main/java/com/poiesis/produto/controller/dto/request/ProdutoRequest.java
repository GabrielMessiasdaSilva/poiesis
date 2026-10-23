package com.poiesis.produto.controller.dto.request;

import com.poiesis.produto.entity.Categoria;
import java.math.BigDecimal;

public class ProdutoRequest {
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Categoria categoria;
    private String imagemUrl;

    public ProdutoRequest() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
}