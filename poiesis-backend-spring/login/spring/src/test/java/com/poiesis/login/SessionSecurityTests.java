package com.poiesis.login;

import com.poiesis.login.entity.Role;
import com.poiesis.login.repository.jpa.UsuarioRepositoryWithJpa;
import com.poiesis.login.repository.orm.UsuarioOrmJpa;
import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

/** Verifica o contrato privado; assinatura e expiração são testadas no gateway. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:auth_test", "application.jwt.expiration=60000"
})
class SessionSecurityTests {
    @LocalServerPort int port;
    @Autowired
    UsuarioRepositoryWithJpa users;
    HttpResponse<String> request(String method, String path, String body, String... headers) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (headers.length > 0) builder.headers(headers);
        if (body != null) builder.header("Content-Type", "application/json");
        return HttpClient.newHttpClient().send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private HttpResponse<String> check(String token, String email, String roles) throws Exception {
        return request("POST", "/internal/sessions/validate", "{\"token\":\"" + token + "\",\"email\":\""
                + email + "\",\"roles\":" + roles + "}");
    }
    @Test void logoutPersistsRevocationAndSessionUsesGatewayIdentity() throws Exception {
        String token = "validated-token", expires = Long.toString(Instant.now().plusSeconds(60).toEpochMilli());
        assertEquals(204, check(token, "admin@poiesis.com", "[\"ADMIN\",\"USER\"]").statusCode());
        var session = request("GET", "/v1/auth/session", null, "X-User-Email", "admin@poiesis.com",
                "X-User-Roles", "ADMIN,USER", "X-Token-Expires-At", expires);
        assertEquals(200, session.statusCode());
        assertTrue(session.body().contains("admin@poiesis.com"));
        assertTrue(session.body().contains(expires));
        assertEquals(204, request("POST", "/v1/auth/logout", null, "Authorization", "Bearer " + token,
                "X-Token-Expires-At", expires).statusCode());
        assertEquals(401, check(token, "admin@poiesis.com", "[\"ADMIN\",\"USER\"]").statusCode());
        assertEquals(204, check("fresh-token", "admin@poiesis.com", "[\"ADMIN\",\"USER\"]").statusCode());
    }
    @Test void rejectsMissingOrChangedUsersAndDisabledAccounts() throws Exception {
        assertEquals(401, check("unknown", "missing@test.com", "[\"USER\"]").statusCode());
        assertEquals(401, check("roles", "admin@poiesis.com", "[\"USER\"]").statusCode());
        users.save(new UsuarioOrmJpa(null, "Disabled",
                "disabled@test.com", "unused", false, Set.of(Role.USER)));
        assertEquals(401, check("disabled", "disabled@test.com", "[\"USER\"]").statusCode());
        assertEquals(400, check("", "admin@poiesis.com", "[]").statusCode());
    }
    @Test void registrationValidatesFieldsAndRejectsDuplicateEmail() throws Exception {
        assertEquals(401, request("POST", "/v1/auth/login",
                "{\"email\":\"unknown@test.com\",\"senha\":\"wrong\"}").statusCode());
        assertEquals(400, request("POST", "/v1/auth/register",
                "{\"nome\":\"\",\"email\":\"invalid\",\"senha\":\"short\"}").statusCode());
        String body = "{\"nome\":\"Novo Usuário\",\"email\":\"register@test.com\",\"senha\":\"strong-password\",\"roles\":[\"ADMIN\"]}";
        var created = request("POST", "/v1/auth/register", body);
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("USER"));
        assertFalse(created.body().contains("ADMIN"));
        assertEquals(409, request("POST", "/v1/auth/register", body).statusCode());
        var first = request("POST", "/v1/auth/login", "{\"email\":\"register@test.com\",\"senha\":\"strong-password\"}");
        var second = request("POST", "/v1/auth/login", "{\"email\":\"register@test.com\",\"senha\":\"strong-password\"}");
        assertEquals(200, first.statusCode());
        assertEquals(200, second.statusCode());
        assertNotEquals(first.body(), second.body());
        assertEquals(401, request("POST", "/v1/auth/login", "{\"email\":\"register@test.com\",\"senha\":\"wrong\"}").statusCode());
    }
    @Test void browserLoginIssuesHttpOnlyCookieAndLogoutDeletesIt() throws Exception {
        var response = request("POST", "/v1/auth/login",
                "{\"email\":\"admin@poiesis.com\",\"senha\":\"admin123\"}", "X-Auth-Mode", "cookie");
        assertEquals(200, response.statusCode());
        assertFalse(response.body().contains("token"));
        String header = response.headers().firstValue("set-cookie").orElseThrow();
        assertTrue(header.contains("poiesis_session="));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=Lax"));
        assertTrue(header.contains("Path=/v1"));
        assertTrue(header.contains("Max-Age=60"));
        assertFalse(header.contains("Domain="));
        String token = java.net.HttpCookie.parse(header).getFirst().getValue();
        String claims = new String(java.util.Base64.getUrlDecoder().decode(token.split("\\.")[1]),
                java.nio.charset.StandardCharsets.UTF_8);
        java.util.List<String> roles = com.jayway.jsonpath.JsonPath.read(claims, "$.roles");
        assertEquals(Set.of("ADMIN", "USER"), new java.util.HashSet<>(roles));
        assertEquals(204, check(token, "admin@poiesis.com", "[\"ADMIN\",\"USER\"]").statusCode());
        var logout = request("POST", "/v1/auth/logout", null, "Authorization", "Bearer " + token,
                "X-Token-Expires-At", Long.toString(Instant.now().plusSeconds(60).toEpochMilli()));
        assertEquals(204, logout.statusCode());
        String deleted = logout.headers().firstValue("set-cookie").orElseThrow();
        assertTrue(deleted.contains("poiesis_session="));
        assertTrue(deleted.contains("Max-Age=0"));
        assertTrue(deleted.contains("Path=/v1"));
        assertEquals(401, check(token, "admin@poiesis.com", "[\"ADMIN\",\"USER\"]").statusCode());
    }

    @Test void secureCookieCanBeEnabledForHttps() {
        var cookies = new com.poiesis.login.controller.SessionCookie(new org.springframework.mock.env.MockEnvironment()
                .withProperty("application.jwt.expiration", "60000").withProperty("application.auth.cookie.secure", "true"));
        assertTrue(cookies.create("test").contains("Secure"));
        assertTrue(cookies.clear().contains("Secure"));
    }
}
