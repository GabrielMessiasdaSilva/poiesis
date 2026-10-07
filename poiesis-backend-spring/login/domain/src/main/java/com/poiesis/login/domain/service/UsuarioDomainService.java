package com.poiesis.login.domain.service;

import com.poiesis.login.domain.entity.Role;
import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.domain.repository.UsuarioRepositoryPort;

public class UsuarioDomainService {

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public UsuarioDomainService(UsuarioRepositoryPort usuarioRepositoryPort) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    public Usuario cadastrarNovoUsuario(Usuario usuario) {
        if (usuarioRepositoryPort.existePorEmail(usuario.getEmail())) {
            throw new EmailAlreadyRegisteredException();
        }

        // A role padrão é definida no servidor; o cadastro não escolhe privilégios.
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.adicionarRole(Role.USER);
        }

        return usuarioRepositoryPort.salvar(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepositoryPort.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o e-mail: " + email));
    }
}
