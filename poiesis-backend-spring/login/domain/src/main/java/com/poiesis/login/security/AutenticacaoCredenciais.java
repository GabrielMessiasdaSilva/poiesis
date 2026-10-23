package com.poiesis.login.security;

import com.poiesis.login.entity.UsuarioAutenticado;

public interface AutenticacaoCredenciais {
    UsuarioAutenticado autenticar(String email, String senha);
}
