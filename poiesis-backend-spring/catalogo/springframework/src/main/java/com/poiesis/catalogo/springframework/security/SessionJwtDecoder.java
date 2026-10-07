package com.poiesis.catalogo.springframework.security;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
/** Consulta no login as revogações de sessão persistidas após o logout. */
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
        // Primeiro valida o JWT; depois confirma se a sessão continua aceita pelo login.
        Jwt jwt = delegate.decode(token);
        try { login.get().uri("/v1/auth/session").headers(h -> h.setBearerAuth(token)).retrieve().toBodilessEntity(); }
        // Se o login falhar ou recusar a sessão, o acesso também é recusado.
        catch (RestClientException e) { throw new BadJwtException("Sessão inválida ou indisponível.", e); }
        return jwt;
    }
}
