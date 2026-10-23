package com.poiesis.login.controller.adapter;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.controller.dto.request.RegisterRequest;
import com.poiesis.login.controller.dto.response.UsuarioResponse;

public class UsuarioAdapterController {

    public static Usuario toDomain(RegisterRequest dto) {
        return new Usuario(null, dto.getNome(), dto.getEmail(), dto.getSenha(), true, null);
    }

    public static UsuarioResponse toResponse(Usuario domain) {
        return new UsuarioResponse(domain.getId(), domain.getNome(), domain.getEmail(), domain.getRoles());
    }
}
