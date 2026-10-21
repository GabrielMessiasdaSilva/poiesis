package com.poiesis.login.security;

import com.poiesis.login.security.CriptografiaSenha;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CriptografiaSenhaBCrypt implements CriptografiaSenha {
    private final PasswordEncoder passwords;
    public CriptografiaSenhaBCrypt(PasswordEncoder passwords) { this.passwords = passwords; }
    @Override public String gerarHash(String senha) { return passwords.encode(senha); }
}
