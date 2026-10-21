package com.poiesis.login.repository.adapter;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.repository.orm.UsuarioOrmJpa;

public class UsuarioRepositoryAdapter {

    public static Usuario toDomain(UsuarioOrmJpa entity) {
        return new Usuario(entity.getId(), entity.getNome(), entity.getEmail(), entity.getSenha(), entity.getAtivo(), entity.getRoles());
    }

    public static UsuarioOrmJpa toOrm(Usuario domain) {
        return new UsuarioOrmJpa(domain.getId(), domain.getNome(), domain.getEmail(), domain.getSenha(), domain.getAtivo(), domain.getRoles());
    }
}
