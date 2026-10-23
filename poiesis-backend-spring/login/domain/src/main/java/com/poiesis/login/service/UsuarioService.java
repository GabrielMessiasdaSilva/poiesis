package com.poiesis.login.service;

import com.poiesis.login.entity.Role;
import com.poiesis.login.entity.Usuario;
import com.poiesis.login.exception.EmailAlreadyRegisteredException;
import com.poiesis.login.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarNovoUsuario(Usuario usuario) {
        if (usuarioRepository.existePorEmail(usuario.getEmail())) {
            throw new EmailAlreadyRegisteredException();
        }

        // A role padrão é definida no servidor; o cadastro não escolhe privilégios.
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            usuario.adicionarRole(Role.USER);
        }

        return usuarioRepository.salvar(usuario);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.buscarPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o e-mail: " + email));
    }
}
