package com.poiesis.relatorio;

import com.poiesis.relatorio.integration.ProducaoIntegrationImpl;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class ProducaoIntegrationTests {
    private HttpServer server;
    private ProducaoIntegrationImpl integration;
    private int status = 200;
    private String body = """
            {"emPendente":2,"emCorte":3,"emCostura":4,"emAcabamento":5,"concluidos":6}
            """;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/producao/resumo", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        integration = new ProducaoIntegrationImpl(new MockEnvironment().withProperty(
                "application.producao.url", "http://127.0.0.1:" + server.getAddress().getPort()));
    }

    @AfterEach void stop() { server.stop(0); }

    @Test void leContratoHttpDoResumoDeProducao() {
        var resumo = integration.consultarResumo();
        assertEquals(2L, resumo.emPendente());
        assertEquals(3L, resumo.emCorte());
        assertEquals(4L, resumo.emCostura());
        assertEquals(5L, resumo.emAcabamento());
        assertEquals(6L, resumo.concluidos());
    }

    @Test void traduzFalhaHttpParaIndisponibilidade() {
        status = 500;
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, integration::consultarResumo).getStatusCode());
    }

    @Test void rejeitaRespostaVaziaOuJsonInvalido() {
        body = "";
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, integration::consultarResumo).getStatusCode());
        body = "resposta inválida";
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE,
                assertThrows(ResponseStatusException.class, integration::consultarResumo).getStatusCode());
    }
}
