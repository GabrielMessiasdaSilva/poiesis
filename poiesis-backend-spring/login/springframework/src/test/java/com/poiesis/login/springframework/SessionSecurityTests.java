package com.poiesis.login.springframework;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import com.poiesis.login.springframework.security.JwtService;
import org.springframework.security.core.userdetails.User;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.datasource.url=jdbc:h2:mem:auth_test", "application.jwt.expiration=60000"
})
class SessionSecurityTests {
    @LocalServerPort int port;
    @Autowired JwtService jwt;
    @Autowired org.springframework.core.env.Environment env;
    HttpResponse<String> request(String method, String path, String token, String body) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path));
        if (token != null) b.header("Authorization", "Bearer " + token);
        if (body != null) b.header("Content-Type", "application/json");
        return HttpClient.newHttpClient().send(b.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    @Test void loginAndLogoutInvalidateToken() throws Exception {
        assertEquals(401, request("GET", "/v1/auth/session", null, null).statusCode());
        assertEquals(401, request("POST", "/v1/auth/login", null,
            "{\"email\":\"unknown@test.com\",\"senha\":\"wrong\"}").statusCode());
        var token = jwt.gerarToken(User.withUsername("admin@poiesis.com").password("unused").roles("ADMIN", "USER").build());
        assertEquals(200, request("GET", "/v1/auth/session", token, null).statusCode());
        assertEquals(204, request("POST", "/v1/auth/logout", token, null).statusCode());
        assertEquals(401, request("GET", "/v1/auth/session", token, null).statusCode());
        var fresh = jwt.gerarToken(User.withUsername("admin@poiesis.com").password("unused").roles("ADMIN", "USER").build());
        assertNotEquals(token, fresh);
        assertEquals(200, request("GET", "/v1/auth/session", fresh, null).statusCode());
    }
    @Test void registrationValidatesFieldsAndRejectsDuplicateEmail() throws Exception {
        assertEquals(400, request("POST", "/v1/auth/register", null,
            "{\"nome\":\"\",\"email\":\"invalid\",\"senha\":\"short\"}").statusCode());
        var body = "{\"nome\":\"Novo Usuário\",\"email\":\"register@test.com\",\"senha\":\"strong-password\",\"roles\":[\"ADMIN\"]}";
        var created = request("POST", "/v1/auth/register", null, body);
        assertEquals(201, created.statusCode());
        assertTrue(created.body().contains("USER"));
        assertFalse(created.body().contains("ADMIN"));
        assertEquals(409, request("POST", "/v1/auth/register", null, body).statusCode());
        assertEquals(200, request("POST", "/v1/auth/login", null,
            "{\"email\":\"register@test.com\",\"senha\":\"strong-password\"}").statusCode());
    }
    @Test void expiredTokenIsRejectedImmediately() throws Exception {
        var expiredJwt = new JwtService(new org.springframework.mock.env.MockEnvironment()
            .withProperty("application.jwt.secret", env.getRequiredProperty("application.jwt.secret"))
            .withProperty("application.jwt.expiration", "-1000"));
        var token = expiredJwt.gerarToken(User.withUsername("admin@poiesis.com").password("unused").roles("ADMIN", "USER").build());
        assertEquals(401, request("GET", "/v1/auth/session", token, null).statusCode());
    }
    @Test void removedUserAndChangedRolesCannotUseToken() throws Exception {
        var unknown = jwt.gerarToken(User.withUsername("missing@test.com").password("unused").roles("USER").build());
        assertEquals(401, request("GET", "/v1/auth/session", unknown, null).statusCode());
        var wrongRoles = jwt.gerarToken(User.withUsername("admin@poiesis.com").password("unused").roles("USER").build());
        assertEquals(401, request("GET", "/v1/auth/session", wrongRoles, null).statusCode());
    }
}
