package com.poiesis.login.springframework.security;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.domain.service.UsuarioDomainService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioDomainService usuarioDomainService;

    public UserDetailsServiceImpl(UsuarioDomainService usuarioDomainService) {
        this.usuarioDomainService = usuarioDomainService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioDomainService.buscarPorEmail(email);

        List<SimpleGrantedAuthority> authorities = usuario.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        return new User(usuario.getEmail(), usuario.getSenha(), usuario.getAtivo(), true, true, true, authorities);
    }
}
