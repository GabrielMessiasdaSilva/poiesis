package com.poiesis.pedido;

import com.poiesis.pedido.integration.CatalogoIntegrationImpl;
import com.poiesis.pedido.controller.ApiExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import com.poiesis.pedido.integration.CustomizacaoIntegrationImpl;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CatalogoIntegrationImplTests {
    @Test
    void lePrecoBaseDoContratoRealDoCatalogo() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CatalogoIntegrationImpl integration = new CatalogoIntegrationImpl(builder,
                new MockEnvironment().withProperty("application.config.produto-url", "http://produto"));
        server.expect(requestTo("http://produto/v1/produtos/7"))
                .andRespond(withSuccess("""
                    {"id":7,"nome":"Camiseta","descricao":"Algodão","precoBase":49.90,"categoria":"CAMISETA","ativo":true}
                    """, MediaType.APPLICATION_JSON));
        var produto = integration.buscarProduto(7L);
        assertEquals(7L, produto.id());
        assertEquals("Camiseta", produto.nome());
        assertEquals(new BigDecimal("49.90"), produto.preco());
        server.verify();
    }

    @Test
    void consultaCustomizacoesNoMesmoEnderecoConfiguradoParaProduto() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        var integration = new CustomizacaoIntegrationImpl(builder,
                new MockEnvironment().withProperty("application.config.produto-url", "http://produto"));
        server.expect(requestTo("http://produto/v1/customizacoes/produto/7"))
                .andRespond(withSuccess("""
                    [{"id":3,"produtoId":7,"tipo":"COR","nome":"Azul","precoAdicional":5.00,"ativo":true},
                     {"id":4,"produtoId":7,"tipo":"COR","nome":"Verde","precoAdicional":0.00,"ativo":false}]
                    """, MediaType.APPLICATION_JSON));
        var opcoes = integration.listarAtivas(7L);
        assertEquals(1, opcoes.size());
        assertEquals("Azul", opcoes.getFirst().nome());
        assertEquals(new BigDecimal("5.00"), opcoes.getFirst().precoAdicional());
        server.verify();
    }

    @Test
    void produtoInexistenteEhErroDoPedidoEIndisponibilidadeContinua503() {
        for (var status : java.util.List.of(HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND, HttpStatus.INTERNAL_SERVER_ERROR)) {
            RestClient.Builder builder = RestClient.builder();
            MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
            var integration = new CatalogoIntegrationImpl(builder,
                    new MockEnvironment().withProperty("application.config.produto-url", "http://produto"));
            server.expect(requestTo("http://produto/v1/produtos/999"))
                    .andRespond(withStatus(status));
            var exception = assertThrows(RestClientResponseException.class, () -> integration.buscarProduto(999L));
            var response = new ApiExceptionHandler().upstreamResponse(exception);
            assertEquals(status == HttpStatus.INTERNAL_SERVER_ERROR ? 503 : 400, response.getStatusCode().value());
            server.verify();
        }
    }
}
