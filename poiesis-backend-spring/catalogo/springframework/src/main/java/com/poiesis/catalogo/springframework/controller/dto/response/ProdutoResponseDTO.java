package com.poiesis.catalogo.springframework.controller.dto.response;

import com.poiesis.catalogo.domain.entity.Categoria;
import java.math.BigDecimal;

public class ProdutoResponseDTO {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoBase;
    private Categoria categoria;
    private Boolean ativo;

    public ProdutoResponseDTO() {}

    public ProdutoResponseDTO(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.categoria = categoria;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public Categoria getCategoria() { return categoria; }
    public Boolean getAtivo() { return ativo; }
}