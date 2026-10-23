package com.poiesis.pedido.repository.orm;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class CustomizacaoEscolhidaOrmJpa {
    private Long customizacaoId;
    private String tipo;
    private String nome;
    private BigDecimal precoAdicional;

    public CustomizacaoEscolhidaOrmJpa() {}

    public CustomizacaoEscolhidaOrmJpa(Long customizacaoId, String tipo, String nome, BigDecimal precoAdicional) {
        this.customizacaoId = customizacaoId;
        this.tipo = tipo;
        this.nome = nome;
        this.precoAdicional = precoAdicional;
    }

    public Long getCustomizacaoId() { return customizacaoId; }
    public String getTipo() { return tipo; }
    public String getNome() { return nome; }
    public BigDecimal getPrecoAdicional() { return precoAdicional; }
}
