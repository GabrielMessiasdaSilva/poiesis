package com.poiesis.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/** O navegador usa cookie; clientes nativos e ferramentas podem continuar usando Bearer. */
public class CookieBearerTokenResolver implements BearerTokenResolver {
    public static final String COOKIE_NAME = "poiesis_session";
    private final DefaultBearerTokenResolver bearer = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String token = bearer.resolve(request);
        if (token != null) return token;
        // Um cookie antigo não deve impedir um novo login nem a obtenção do token CSRF.
        if (request.getRequestURI().equals("/v1/auth/login")
                || request.getRequestURI().equals("/v1/auth/register")
                || request.getRequestURI().equals("/v1/auth/csrf")) return null;
        if (request.getCookies() == null) return null;
        for (var cookie : request.getCookies()) {
            if (!COOKIE_NAME.equals(cookie.getName())) continue;
            if (token != null) throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request"));
            if (!cookie.getValue().isBlank()) token = cookie.getValue();
        }
        return token;
    }
}
