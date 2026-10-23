package com.poiesis.produto;

import com.jayway.jsonpath.JsonPath;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProdutoApiTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void produtoECustomizacaoFuncionamNaMesmaAplicacao() throws Exception {
        var produto = request("POST", "/v1/produtos", """
                {"nome":"Camisa API", "descricao":"Teste integrado", "precoBase":30.00,"categoria":"CAMISA"}
                """);
        assertEquals(201, produto.statusCode(), produto.body());
        Number produtoId = JsonPath.read(produto.body(), "$.id");
        String opcao = """
                {"produtoId":%s,"tipo":"COR","nome":"Azul","precoAdicional":5.00}
                """.formatted(produtoId);
        var criada = request("POST", "/v1/customizacoes", opcao);
        assertEquals(201, criada.statusCode(), criada.body());
        Number opcaoId = JsonPath.read(criada.body(), "$.id");
        var lista = request("GET", "/v1/customizacoes/produto/" + produtoId, null);
        assertEquals(200, lista.statusCode());
        assertEquals("Azul", JsonPath.read(lista.body(), "$[0].nome"));
        assertEquals(200, request("GET", "/v1/produtos/" + produtoId, null).statusCode());

        assertEquals(200, request("PUT", "/v1/customizacoes/" + opcaoId,
                opcao.replace("Azul", "Verde")).statusCode());
        assertEquals(204, request("DELETE", "/v1/customizacoes/" + opcaoId, null).statusCode());
        assertEquals("[]", request("GET", "/v1/customizacoes/produto/" + produtoId, null).body());
    }

    @Test
    void customizacaoRejeitaProdutoInexistenteSemPersistir() throws Exception {
        String opcao = """
                {"produtoId":9223372036854775807,"tipo":"COR","nome":"Azul","precoAdicional":5.00}
                """;
        var resposta = request("POST", "/v1/customizacoes", opcao);
        assertEquals(400, resposta.statusCode(), resposta.body());
        assertNotNull(JsonPath.read(resposta.body(), "$.erro"));
        assertEquals("[]", request("GET", "/v1/customizacoes/produto/9223372036854775807", null).body());
    }
}
