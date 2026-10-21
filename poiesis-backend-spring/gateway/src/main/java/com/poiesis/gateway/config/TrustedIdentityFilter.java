package com.poiesis.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/** Remove identidades fornecidas pelo cliente e encaminha apenas claims validadas. */
public class TrustedIdentityFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Map<String, String> identity = new LinkedHashMap<>();
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken authenticated) {
            var jwt = authenticated.getToken();
            identity.put("Authorization", "Bearer " + jwt.getTokenValue());
            identity.put("X-User-Email", jwt.getSubject());
            identity.put("X-User-Roles", String.join(",", jwt.getClaimAsStringList("roles")));
            identity.put("X-Token-Expires-At", Long.toString(jwt.getExpiresAt().toEpochMilli()));
        }
        chain.doFilter(new HttpServletRequestWrapper(request) {
            private boolean reserved(String name) {
                return name.equalsIgnoreCase("Authorization") || name.equalsIgnoreCase("X-User-Email") || name.equalsIgnoreCase("X-User-Roles")
                        || name.equalsIgnoreCase("X-Token-Expires-At");
            }

            @Override public String getHeader(String name) {
                if (!reserved(name)) return super.getHeader(name);
                return identity.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(name))
                        .map(Map.Entry::getValue).findFirst().orElse(null);
            }

            @Override public Enumeration<String> getHeaders(String name) {
                if (!reserved(name)) return super.getHeaders(name);
                String value = getHeader(name);
                return Collections.enumeration(value == null ? List.of() : List.of(value));
            }

            @Override public Enumeration<String> getHeaderNames() {
                var names = Collections.list(super.getHeaderNames());
                names.removeIf(this::reserved);
                names.addAll(identity.keySet());
                return Collections.enumeration(names);
            }
        }, response);
    }
}
