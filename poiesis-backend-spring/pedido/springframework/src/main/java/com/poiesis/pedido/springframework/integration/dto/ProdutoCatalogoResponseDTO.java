package com.poiesis.pedido.springframework.integration.dto;

import java.math.BigDecimal;

public class ProdutoCatalogoResponseDTO {
    private Long id;
    private String nome;
    private BigDecimal precoBase;

    public ProdutoCatalogoResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
}
