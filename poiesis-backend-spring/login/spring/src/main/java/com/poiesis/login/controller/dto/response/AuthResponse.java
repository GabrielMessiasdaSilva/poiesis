package com.poiesis.login.controller.dto.response;

public class AuthResponse {
    private String token;
    private String tipo = "Bearer";

    public AuthResponse() {}

    public AuthResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
}