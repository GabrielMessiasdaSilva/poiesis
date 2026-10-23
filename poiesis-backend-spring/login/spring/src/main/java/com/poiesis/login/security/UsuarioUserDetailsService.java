package com.poiesis.login.security;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.service.UsuarioService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioUserDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    public UsuarioUserDetailsService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario;
        try { usuario = usuarioService.buscarPorEmail(email); }
        catch (RuntimeException e) { throw new UsernameNotFoundException("Usuário não encontrado.", e); }

        List<SimpleGrantedAuthority> authorities = usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return new User(usuario.getEmail(), usuario.getSenha(), usuario.getAtivo(), true, true, true, authorities);
    }
}
