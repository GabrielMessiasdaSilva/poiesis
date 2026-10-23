package com.poiesis.login.service;

import com.poiesis.login.security.AutenticacaoCredenciais;
import com.poiesis.login.security.EmissorToken;

public class AutenticacaoService {
    private final AutenticacaoCredenciais credenciais;
    private final EmissorToken tokens;

    public AutenticacaoService(AutenticacaoCredenciais credenciais, EmissorToken tokens) {
        this.credenciais = credenciais;
        this.tokens = tokens;
    }

    public String autenticar(String email, String senha) { return tokens.emitir(credenciais.autenticar(email, senha)); }
}
