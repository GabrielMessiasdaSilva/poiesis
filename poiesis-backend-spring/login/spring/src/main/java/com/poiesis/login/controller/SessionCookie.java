package com.poiesis.login.controller;

import java.time.Duration;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Representação HTTP da sessão; não interpreta nem valida o JWT. */
@Component
public class SessionCookie {
    private final boolean secure;
    private final Duration lifetime;

    public SessionCookie(Environment environment) {
        secure = environment.getProperty("application.auth.cookie.secure", Boolean.class, false);
        lifetime = Duration.ofMillis(environment.getRequiredProperty("application.jwt.expiration", Long.class));
    }

    public String create(String token) { return cookie(token, lifetime); }
    public String clear() { return cookie("", Duration.ZERO); }

    private String cookie(String value, Duration maxAge) {
        return ResponseCookie.from("poiesis_session", value).httpOnly(true).secure(secure)
                .sameSite("Lax").path("/v1").maxAge(maxAge).build().toString();
    }
}
