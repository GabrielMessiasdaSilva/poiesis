package com.poiesis.login.config;

import com.poiesis.login.repository.SessaoRepository;
import com.poiesis.login.repository.UsuarioRepository;
import com.poiesis.login.security.AutenticacaoCredenciais;
import com.poiesis.login.security.CriptografiaSenha;
import com.poiesis.login.security.EmissorToken;
import com.poiesis.login.service.AutenticacaoService;
import com.poiesis.login.service.CadastroUsuarioService;
import com.poiesis.login.service.SessaoService;
import com.poiesis.login.service.UsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public UsuarioService usuarioService(UsuarioRepository usuarioRepository) {
        return new UsuarioService(usuarioRepository);
    }
    @Bean
    public CadastroUsuarioService cadastrarUsuario(
            UsuarioService usuarios, CriptografiaSenha senhas) {
        return new CadastroUsuarioService(usuarios, senhas);
    }

    @Bean
    public AutenticacaoService autenticarUsuario(
            AutenticacaoCredenciais credenciais, EmissorToken tokens) {
        return new AutenticacaoService(credenciais, tokens);
    }

    @Bean
    public SessaoService sessaoService(UsuarioRepository usuarios, SessaoRepository sessoes) {
        return new SessaoService(usuarios, sessoes);
    }
}
