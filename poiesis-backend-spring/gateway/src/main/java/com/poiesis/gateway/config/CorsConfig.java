package com.poiesis.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;

    public CorsConfig(org.springframework.core.env.Environment environment) {
        allowedOrigins = environment.getRequiredProperty("application.cors.allowed-origins").split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Auth-Mode", "X-CSRF-TOKEN")
                .exposedHeaders("Authorization")
                .maxAge(3600)
                .allowCredentials(true);
    }
}
