package com.poiesis.pedido.integration;

import com.poiesis.pedido.integration.CatalogoIntegration;
import com.poiesis.pedido.integration.dto.ProdutoCatalogoResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogoIntegrationImpl implements CatalogoIntegration {
    private final RestClient restClient;

    public CatalogoIntegrationImpl(RestClient.Builder builder, Environment environment) {
        this.restClient = builder.baseUrl(environment.getRequiredProperty("application.config.produto-url")).build();
    }

    @Override
    public Produto buscarProduto(Long produtoId) {
        ProdutoCatalogoResponse produto = restClient.get()
                .uri("/v1/produtos/{id}", produtoId)
                .retrieve()
                .body(ProdutoCatalogoResponse.class);
        if (produto == null) return null;
        return new Produto(produto.getId(), produto.getNome(), produto.getPrecoBase());
    }
}
