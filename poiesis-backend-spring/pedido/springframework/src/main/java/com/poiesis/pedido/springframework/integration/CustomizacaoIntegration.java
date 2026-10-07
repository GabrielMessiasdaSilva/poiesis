package com.poiesis.pedido.springframework.integration;

import com.poiesis.pedido.domain.entity.CustomizacaoEscolhida;
import com.poiesis.pedido.domain.port.CustomizacaoServicePort;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CustomizacaoIntegration implements CustomizacaoServicePort {
    private final RestClient client;

    public CustomizacaoIntegration(RestClient.Builder builder, Environment env) {
        client = builder.baseUrl(env.getProperty("application.config.customizacao-url", "http://localhost:8086")).build();
    }

    @Override
    public List<CustomizacaoEscolhida> listarAtivas(Long produtoId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalArgumentException("Sessão necessária para escolher customizações.");
        }
        var options = client.get().uri("/v1/customizacoes/produto/{id}", produtoId)
                .headers(headers -> headers.setBearerAuth(jwt.getTokenValue()))
                .retrieve().body(Opcao[].class);
        return options == null ? List.of() : Arrays.stream(options)
                .filter(o -> Boolean.TRUE.equals(o.ativo()) && produtoId.equals(o.produtoId()))
                .map(o -> new CustomizacaoEscolhida(o.id(), o.tipo(), o.nome(), o.precoAdicional())).toList();
    }

    public record Opcao(Long id, Long produtoId, String tipo, String nome, BigDecimal precoAdicional, Boolean ativo) {}
}
