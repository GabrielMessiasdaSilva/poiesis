package com.poiesis.catalogo.springframework.repository.entity;

import com.poiesis.catalogo.domain.entity.Categoria;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_produtos")
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Controle otimista: o JPA detecta versões divergentes ao atualizar a mesma entidade.
    @Version
    private Long version;

    private String nome;
    private String descricao;
    private BigDecimal precoBase;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    private Boolean ativo;

    public ProdutoEntity() {}

    public ProdutoEntity(Long id, String nome, String descricao, BigDecimal precoBase, Categoria categoria, Boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoBase = precoBase;
        this.categoria = categoria;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public BigDecimal getPrecoBase() { return precoBase; }
    public void setPrecoBase(BigDecimal precoBase) { this.precoBase = precoBase; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
