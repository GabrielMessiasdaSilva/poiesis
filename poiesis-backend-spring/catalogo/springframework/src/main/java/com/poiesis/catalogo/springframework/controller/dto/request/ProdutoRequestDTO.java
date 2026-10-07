package com.poiesis.catalogo.springframework.controller.dto.request;

import com.poiesis.catalogo.domain.entity.Categoria;
import java.math.BigDecimal;

public class ProdutoRequestDTO {
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Categoria categoria;

    public ProdutoRequestDTO() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
}