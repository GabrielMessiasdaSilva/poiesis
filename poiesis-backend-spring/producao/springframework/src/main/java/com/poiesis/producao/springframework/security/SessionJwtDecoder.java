package com.poiesis.producao.springframework.security;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
/** Checks centrally persisted logout revocations, including direct service access. */
public class SessionJwtDecoder implements JwtDecoder {
    private final JwtDecoder delegate;
    private final RestClient login;
    public SessionJwtDecoder(JwtDecoder delegate, String loginUrl) {
        this.delegate = delegate;
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);
        this.login = RestClient.builder().baseUrl(loginUrl).requestFactory(factory).build();
    }
    @Override public Jwt decode(String token) {
        Jwt jwt = delegate.decode(token);
        try { login.get().uri("/v1/auth/session").headers(h -> h.setBearerAuth(token)).retrieve().toBodilessEntity(); }
        catch (RestClientException e) { throw new BadJwtException("Sessão inválida ou indisponível.", e); }
        return jwt;
    }
}
