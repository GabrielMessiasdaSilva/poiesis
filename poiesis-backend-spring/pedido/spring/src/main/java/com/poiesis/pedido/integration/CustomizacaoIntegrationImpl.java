package com.poiesis.pedido.integration;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import com.poiesis.pedido.integration.CustomizacaoIntegration;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomizacaoIntegrationImpl implements CustomizacaoIntegration {
    private final RestClient client;

    public CustomizacaoIntegrationImpl(RestClient.Builder builder, Environment env) {
        client = builder.baseUrl(env.getRequiredProperty("application.config.produto-url")).build();
    }

    @Override
    public List<CustomizacaoEscolhida> listarAtivas(Long produtoId) {
        var options = client.get().uri("/v1/customizacoes/produto/{id}", produtoId)
                .retrieve().body(Opcao[].class);
        return options == null ? List.of() : Arrays.stream(options)
                .filter(o -> Boolean.TRUE.equals(o.ativo()) && produtoId.equals(o.produtoId()))
                .map(o -> new CustomizacaoEscolhida(o.id(), o.tipo(), o.nome(), o.precoAdicional())).toList();
    }

    public record Opcao(Long id, Long produtoId, String tipo, String nome, BigDecimal precoAdicional, Boolean ativo) {}
}
