package com.poiesis.login.springframework.controller.dto.response;

public class AuthResponseDTO {
    private String token;
    private String tipo = "Bearer";

    public AuthResponseDTO() {}

    public AuthResponseDTO(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
}