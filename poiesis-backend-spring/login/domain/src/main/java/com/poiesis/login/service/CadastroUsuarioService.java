package com.poiesis.login.service;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.security.CriptografiaSenha;
import com.poiesis.login.service.UsuarioService;

public class CadastroUsuarioService {
    private final UsuarioService usuarios;
    private final CriptografiaSenha senhas;

    public CadastroUsuarioService(UsuarioService usuarios, CriptografiaSenha senhas) {
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    public Usuario cadastrar(Usuario dados) {
        // O cadastro público não aceita perfis escolhidos pelo cliente.
        var usuario = new Usuario(null, dados.getNome().trim(), dados.getEmail().trim(),
                senhas.gerarHash(dados.getSenha()), true, null);
        return usuarios.cadastrarNovoUsuario(usuario);
    }
}
