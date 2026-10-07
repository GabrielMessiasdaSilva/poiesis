package com.poiesis.pedido.springframework;

import com.poiesis.pedido.springframework.integration.CatalogoIntegration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogoIntegrationTests {
    @Test
    void lePrecoBaseDoContratoRealDoCatalogo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CatalogoIntegration integration = new CatalogoIntegration(builder,
                new MockEnvironment().withProperty("application.config.catalogo-url", "http://catalogo"));
        server.expect(requestTo("http://catalogo/v1/produtos/7"))
                .andRespond(withSuccess("""
                    {"id":7,"nome":"Camiseta","descricao":"Algodão","precoBase":49.90,"categoria":"CAMISETA","ativo":true}
                    """, MediaType.APPLICATION_JSON));
        var produto = integration.buscarProduto(7L);
        assertEquals(7L, produto.id());
        assertEquals("Camiseta", produto.nome());
        assertEquals(new BigDecimal("49.90"), produto.preco());
        server.verify();
    }
}
