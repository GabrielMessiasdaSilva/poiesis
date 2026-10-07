package com.poiesis.login.springframework.controller.adapter;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.springframework.controller.dto.request.RegisterRequestDTO;
import com.poiesis.login.springframework.controller.dto.response.UsuarioResponseDTO;
import com.poiesis.login.springframework.repository.entity.UsuarioEntity;

public class UsuarioMapper {

    public static Usuario toDomain(RegisterRequestDTO dto) {
        return new Usuario(null, dto.getNome(), dto.getEmail(), dto.getSenha(), true, null);
    }

    public static Usuario toDomain(UsuarioEntity entity) {
        return new Usuario(entity.getId(), entity.getNome(), entity.getEmail(), entity.getSenha(), entity.getAtivo(), entity.getRoles());
    }

    public static UsuarioEntity toEntity(Usuario domain) {
        return new UsuarioEntity(domain.getId(), domain.getNome(), domain.getEmail(), domain.getSenha(), domain.getAtivo(), domain.getRoles());
    }

    public static UsuarioResponseDTO toResponse(Usuario domain) {
        return new UsuarioResponseDTO(domain.getId(), domain.getNome(), domain.getEmail(), domain.getRoles());
    }
}
