package com.poiesis.gateway.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
public class SecurityConfig {
    @Bean
    JwtDecoder jwtDecoder(Environment environment) {
        String secret = environment.getRequiredProperty("application.jwt.secret");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        // Exige expiração e valida os horários do token sem tolerância adicional.
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
                org.springframework.security.oauth2.jwt.JwtValidators.createDefault(),
                new org.springframework.security.oauth2.jwt.JwtTimestampValidator(java.time.Duration.ZERO),
                new org.springframework.security.oauth2.jwt.JwtClaimValidator<java.time.Instant>("exp", java.util.Objects::nonNull),
                new org.springframework.security.oauth2.jwt.JwtClaimValidator<String>("sub", sub -> sub != null && !sub.isBlank()),
                new org.springframework.security.oauth2.jwt.JwtClaimValidator<Object>("roles", roles ->
                        roles instanceof java.util.List<?> values && !values.isEmpty()
                                && values.stream().allMatch(value -> value instanceof String role && !role.isBlank()))));
        return new SessionJwtDecoder(decoder,
                environment.getProperty("application.login.url", "http://localhost:8081"));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        // Converte os perfis do JWT em permissões ROLE_ usadas pelo Spring Security.
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    org.springframework.security.web.AuthenticationEntryPoint authenticationEntryPoint(Environment environment) {
        var delegate = new org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint();
        boolean secure = environment.getProperty("application.auth.cookie.secure", Boolean.class, false);
        return (request, response, error) -> {
            if (request.getHeader("Authorization") == null && request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if (CookieBearerTokenResolver.COOKIE_NAME.equals(cookie.getName())) {
                        response.addHeader("Set-Cookie", org.springframework.http.ResponseCookie
                                .from(CookieBearerTokenResolver.COOKIE_NAME, "").httpOnly(true).secure(secure)
                                .sameSite("Lax").path("/v1").maxAge(0).build().toString());
                        break;
                    }
                }
            }
            delegate.commence(request, response, error);
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter,
            org.springframework.security.web.AuthenticationEntryPoint authenticationEntryPoint,
            Environment environment) throws Exception {
        var csrfCookies = new org.springframework.security.web.csrf.CookieCsrfTokenRepository();
        csrfCookies.setCookieName("poiesis_csrf");
        csrfCookies.setHeaderName("X-CSRF-TOKEN");
        csrfCookies.setCookieCustomizer(cookie -> cookie.path("/v1").httpOnly(true).sameSite("Lax")
                .secure(environment.getProperty("application.auth.cookie.secure", Boolean.class, false)));
        org.springframework.security.web.util.matcher.RequestMatcher cookieCsrfRequests = request -> {
            if (java.util.Set.of("GET", "HEAD", "OPTIONS", "TRACE").contains(request.getMethod())) return false;
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) return false;
            if ("cookie".equals(request.getHeader("X-Auth-Mode"))) return true;
            return request.getCookies() != null && java.util.Arrays.stream(request.getCookies())
                    .anyMatch(cookie -> CookieBearerTokenResolver.COOKIE_NAME.equals(cookie.getName()));
        };
        return http.cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.csrfTokenRepository(csrfCookies)
                        .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
                        // Resource Server ignora CSRF para qualquer token resolvido. O cookie exige proteção.
                        .requireCsrfProtectionMatcher(cookieCsrfRequests))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/register", "/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/produtos", "/v1/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/producao/pedidos/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/v1/produtos", "/v1/produtos/**",
                                "/v1/producao", "/v1/producao/**",
                                "/v1/relatorios", "/v1/relatorios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/customizacoes", "/v1/customizacoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/customizacoes", "/v1/customizacoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/customizacoes", "/v1/customizacoes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/pedidos").hasRole("USER")
                        .requestMatchers("/v1/pedidos", "/v1/pedidos/**",
                            "/v1/customizacoes", "/v1/customizacoes/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/auth/session").authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/auth/logout").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(new TrustedIdentityFilter(), org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(new CookieBearerTokenResolver())
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }
}
