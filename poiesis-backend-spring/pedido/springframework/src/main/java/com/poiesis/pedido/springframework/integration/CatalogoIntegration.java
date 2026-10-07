package com.poiesis.pedido.springframework.integration;

import com.poiesis.pedido.domain.port.CatalogoServicePort;
import com.poiesis.pedido.springframework.integration.dto.ProdutoCatalogoResponseDTO;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogoIntegration implements CatalogoServicePort {
    private final RestClient restClient;

    public CatalogoIntegration(RestClient.Builder builder, Environment environment) {
        this.restClient = builder.baseUrl(environment.getRequiredProperty("application.config.catalogo-url")).build();
    }

    @Override
    public Produto buscarProduto(Long produtoId) {
        ProdutoCatalogoResponseDTO produto = restClient.get()
                .uri("/v1/produtos/{id}", produtoId)
                .retrieve()
                .body(ProdutoCatalogoResponseDTO.class);
        if (produto == null) return null;
        return new Produto(produto.getId(), produto.getNome(), produto.getPrecoBase());
    }
}
