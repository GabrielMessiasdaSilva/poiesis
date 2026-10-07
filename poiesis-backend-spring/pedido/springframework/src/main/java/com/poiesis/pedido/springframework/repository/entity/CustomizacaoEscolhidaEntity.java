package com.poiesis.pedido.springframework.repository.entity;

import com.poiesis.pedido.domain.entity.CustomizacaoEscolhida;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class CustomizacaoEscolhidaEntity {
    private Long customizacaoId;
    private String tipo;
    private String nome;
    private BigDecimal precoAdicional;

    public CustomizacaoEscolhidaEntity() {}

    public CustomizacaoEscolhidaEntity(CustomizacaoEscolhida opcao) {
        customizacaoId = opcao.id();
        tipo = opcao.tipo();
        nome = opcao.nome();
        precoAdicional = opcao.precoAdicional();
    }

    public CustomizacaoEscolhida toDomain() {
        return new CustomizacaoEscolhida(customizacaoId, tipo, nome, precoAdicional);
    }
}
