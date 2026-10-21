package com.poiesis.login.service;

import com.poiesis.login.exception.SessaoInvalidaException;
import com.poiesis.login.repository.SessaoRepository;
import com.poiesis.login.repository.UsuarioRepository;
import java.time.Instant;
import java.util.List;

/** Confere estado da conta e revogação; a assinatura do JWT pertence ao gateway. */
public class SessaoService {
    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;

    public SessaoService(UsuarioRepository usuarios, SessaoRepository sessoes) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
    }

    public void validar(String token, String email, List<String> roles) {
        if (sessoes.revogada(token)) throw new SessaoInvalidaException();
        var usuario = usuarios.buscarPorEmail(email).orElseThrow(SessaoInvalidaException::new);
        var atuais = usuario.getRoles().stream().map(Enum::name).sorted().toList();
        if (!Boolean.TRUE.equals(usuario.getAtivo()) || !atuais.equals(roles.stream().sorted().toList())) {
            throw new SessaoInvalidaException();
        }
    }
    public void encerrar(String token, Instant expiresAt) { sessoes.revogar(token, expiresAt); }
}
