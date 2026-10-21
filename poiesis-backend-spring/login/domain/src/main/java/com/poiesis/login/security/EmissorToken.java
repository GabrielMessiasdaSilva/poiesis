package com.poiesis.login.security;

import com.poiesis.login.entity.UsuarioAutenticado;

public interface EmissorToken {
    String emitir(UsuarioAutenticado usuario);
}
