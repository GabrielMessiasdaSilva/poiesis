package com.poiesis.gateway;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewaySecurityTests {
    private static final String SECRET = "gateway-test-secret-with-at-least-32-bytes";
    private static final HttpServer upstream;
    private static volatile String revokedToken = "none";
    private static volatile boolean unavailable;
    static {
        try {
            upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            upstream.createContext("/", exchange -> {
                if (exchange.getRequestURI().getPath().equals("/internal/sessions/validate")) {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(unavailable ? 503 : body.contains(revokedToken) ? 401 : 204, -1);
                } else {
                    var headers = exchange.getRequestHeaders();
                    if (exchange.getRequestURI().getPath().equals("/v1/auth/login")
                            && "cookie".equals(headers.getFirst("X-Auth-Mode"))) {
                        exchange.getResponseHeaders().add("Set-Cookie",
                                "poiesis_session=issued-by-login; Path=/v1; HttpOnly; SameSite=Lax");
                    }
                    if (exchange.getRequestURI().getPath().equals("/v1/auth/logout")
                            && headers.getFirst("Authorization") == null) {
                        exchange.sendResponseHeaders(400, -1);
                        exchange.close();
                        return;
                    }
                    byte[] body = (headers.getFirst("X-User-Email") + "|" + headers.getFirst("X-User-Roles")
                            + "|" + headers.getFirst("X-Token-Expires-At")).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                }
                exchange.close();
            });
            upstream.start();
        } catch (java.io.IOException e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        String uri = "http://127.0.0.1:" + upstream.getAddress().getPort();
        registry.add("application.jwt.secret", () -> SECRET);
        registry.add("application.login.url", () -> uri);
        for (String service : List.of("login", "produto", "pedido", "producao", "relatorio")) {
            registry.add("spring.cloud.discovery.client.simple.instances." + service + "-service[0].uri", () -> uri);
        }
    }
    @AfterAll static void stop() { upstream.stop(0); }
    @LocalServerPort int port;
    private HttpResponse<String> request(String method, String path, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("X-User-Email", "forged@test.com").header("x-user-roles", "ADMIN")
                .header("X-Token-Expires-At", "9999999999999");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return HttpClient.newHttpClient().send(builder.method(method, HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }
    private String token(String role, long expiration) throws Exception {
        return sign("{\"sub\":\"user@test.com\",\"roles\":[\"" + role + "\"],\"exp\":" + expiration + "}");
    }
    private String sign(String claims) throws Exception {
        var encoder = Base64.getUrlEncoder().withoutPadding();
        String unsigned = encoder.encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8))
                + "." + encoder.encodeToString(claims.getBytes(StandardCharsets.UTF_8));
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return unsigned + "." + encoder.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }
    @Test void allowsFrontend8086PreflightAndCredentialedReads() throws Exception {
        for (String origin : List.of("http://localhost:8086", "http://127.0.0.1:8086")) {
            var preflight = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/auth/login"))
                    .header("Origin", origin).header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "content-type,x-auth-mode,x-csrf-token")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
            var response = HttpClient.newHttpClient().send(preflight, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals(origin, response.headers().firstValue("access-control-allow-origin").orElseThrow());
            assertEquals("true", response.headers().firstValue("access-control-allow-credentials").orElseThrow());
            var read = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/auth/csrf"))
                    .header("Origin", origin).GET().build();
            var csrf = HttpClient.newHttpClient().send(read, HttpResponse.BodyHandlers.ofString());
            assertEquals(200, csrf.statusCode());
            assertEquals(origin, csrf.headers().firstValue("access-control-allow-origin").orElseThrow());
        }
    }

    @Test void publicRoutesStripForgedIdentityAndProtectedRoutesRequireToken() throws Exception {
        assertEquals("null|null|null", request("GET", "/v1/produtos", null).body());
        assertEquals(200, request("POST", "/v1/auth/login", null).statusCode());
        assertEquals(200, request("POST", "/v1/auth/register", null).statusCode());
        for (String path : List.of("/v1/pedidos", "/v1/producao", "/v1/relatorios/vendas",
                "/v1/customizacoes/produto/1", "/v1/auth/session", "/v1/auth/logout")) {
            assertEquals(401, request("GET", path, null).statusCode(), path);
        }
    }
    @Test void appliesRolesAtGatewayAndReplacesClientIdentity() throws Exception {
        long expires = Instant.now().plusSeconds(120).getEpochSecond();
        String user = token("USER", expires), admin = token("ADMIN", expires);
        var response = request("GET", "/v1/pedidos", user);
        assertEquals(200, response.statusCode());
        assertEquals("user@test.com|USER|" + expires * 1000, response.body());
        for (String[] route : List.of(new String[]{"POST", "/v1/produtos"}, new String[]{"PUT", "/v1/produtos/1"},
                new String[]{"DELETE", "/v1/produtos/1"}, new String[]{"GET", "/v1/producao"},
                new String[]{"PATCH", "/v1/producao/1/status"}, new String[]{"GET", "/v1/relatorios/vendas"},
                new String[]{"POST", "/v1/relatorios/vendas/filtrar"}, new String[]{"PUT", "/v1/customizacoes/1"},
                new String[]{"DELETE", "/v1/customizacoes/1"}, new String[]{"POST", "/v1/customizacoes"})) {
            assertEquals(403, request(route[0], route[1], user).statusCode(), route[1]);
            assertEquals(200, request(route[0], route[1], admin).statusCode(), route[1]);
        }
        assertEquals(403, request("POST", "/v1/customizacoes", user).statusCode());
        assertEquals(200, request("GET", "/v1/customizacoes/produto/1", user).statusCode());
        assertEquals(200, request("GET", "/v1/auth/session", user).statusCode());
        assertEquals(200, request("POST", "/v1/auth/logout", user).statusCode());
        assertEquals(403, request("POST", "/internal/sessions/validate", admin).statusCode());
        assertEquals(403, request("GET", "/v1/auth/unknown", admin).statusCode());
    }
    @Test void rejectsExpiredMalformedAndRevokedTokensAndSessionFailure() throws Exception {
        assertEquals(401, request("GET", "/v1/pedidos", token("USER", Instant.now().minusSeconds(1).getEpochSecond())).statusCode());
        assertEquals(401, request("GET", "/v1/pedidos", "invalid").statusCode());
        assertEquals(401, request("GET", "/v1/pedidos", sign("{\"sub\":\"user@test.com\",\"roles\":[\"USER\"]}")).statusCode());
        for (String claims : List.of(
                "{\"sub\":\"user@test.com\",\"exp\":" + Instant.now().plusSeconds(60).getEpochSecond() + "}",
                "{\"sub\":\"user@test.com\",\"roles\":\"ADMIN\",\"exp\":" + Instant.now().plusSeconds(60).getEpochSecond() + "}",
                "{\"roles\":[\"USER\"],\"exp\":" + Instant.now().plusSeconds(60).getEpochSecond() + "}")) {
            assertEquals(401, request("GET", "/v1/pedidos", sign(claims)).statusCode());
        }
        String valid = token("USER", Instant.now().plusSeconds(180).getEpochSecond());
        String tampered = valid.substring(0, valid.lastIndexOf('.') + 1) + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        assertEquals(401, request("GET", "/v1/pedidos", tampered).statusCode());
        revokedToken = valid;
        try { assertEquals(401, request("GET", "/v1/pedidos", valid).statusCode()); }
        finally { revokedToken = "none"; }
        unavailable = true;
        try { assertEquals(401, request("GET", "/v1/pedidos", valid).statusCode()); }
        finally { unavailable = false; }
    }
    private HttpResponse<String> cookieRequest(String method, String path, String cookies, String csrf) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .header("Origin", "http://localhost:8090").header("X-Auth-Mode", "cookie");
        if (cookies != null) builder.header("Cookie", cookies);
        if (csrf != null) builder.header("X-CSRF-TOKEN", csrf);
        return HttpClient.newHttpClient().send(builder.method(method, HttpRequest.BodyPublishers.noBody()).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @Test void authenticatesCookieAndRequiresCsrfForBrowserMutations() throws Exception {
        String user = token("USER", Instant.now().plusSeconds(120).getEpochSecond());
        String session = "poiesis_session=" + user;
        var read = cookieRequest("GET", "/v1/pedidos", session, null);
        assertEquals(200, read.statusCode());
        assertTrue(read.body().startsWith("user@test.com|USER|"));
        assertEquals("http://localhost:8090", read.headers().firstValue("access-control-allow-origin").orElseThrow());
        assertEquals("true", read.headers().firstValue("access-control-allow-credentials").orElseThrow());
        assertEquals(403, cookieRequest("POST", "/v1/pedidos", session, null).statusCode());
        assertEquals(403, cookieRequest("POST", "/v1/auth/login", null, null).statusCode());
        var bootstrap = cookieRequest("GET", "/v1/auth/csrf", null, null);
        assertEquals(200, bootstrap.statusCode());
        var matcher = java.util.regex.Pattern.compile("\"token\"\\s*:\"([^\"]+)\"").matcher(bootstrap.body());
        assertTrue(matcher.find(), bootstrap.body());
        String csrf = matcher.group(1);
        String csrfHeader = bootstrap.headers().allValues("set-cookie").stream()
                .filter(value -> value.startsWith("poiesis_csrf=")).findFirst().orElseThrow();
        String csrfCookie = csrfHeader.split(";", 2)[0];
        var login = cookieRequest("POST", "/v1/auth/login", csrfCookie, csrf);
        assertEquals(200, login.statusCode());
        assertTrue(login.headers().firstValue("set-cookie").orElseThrow().contains("poiesis_session=issued-by-login"));
        assertEquals(200, cookieRequest("POST", "/v1/pedidos", session + "; " + csrfCookie, csrf).statusCode());
        assertEquals(403, cookieRequest("POST", "/v1/pedidos", session + "; " + csrfCookie, "wrong").statusCode());
        assertEquals(403, cookieRequest("PUT", "/v1/produtos/1", session + "; " + csrfCookie, csrf).statusCode());
        assertEquals(200, cookieRequest("POST", "/v1/auth/logout", session + "; " + csrfCookie, csrf).statusCode());
    }

    @Test void expiredCookieIsDeletedAndCannotBlockNewLogin() throws Exception {
        String expired = "poiesis_session=" + token("USER", Instant.now().minusSeconds(1).getEpochSecond());
        var response = cookieRequest("GET", "/v1/auth/session", expired, null);
        assertEquals(401, response.statusCode());
        assertTrue(response.headers().firstValue("set-cookie").orElseThrow().contains("Max-Age=0"));
        assertEquals(200, cookieRequest("GET", "/v1/auth/csrf", expired, null).statusCode());
        var attacker = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/v1/pedidos"))
                .header("Origin", "http://attacker.test:8090").header("Cookie", expired).GET().build();
        assertEquals(403, HttpClient.newHttpClient().send(attacker, HttpResponse.BodyHandlers.ofString()).statusCode());
    }
}
