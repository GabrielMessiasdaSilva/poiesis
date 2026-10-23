package com.poiesis.login.controller.dto.response;

import com.poiesis.login.entity.Role;
import java.util.Set;

public class UsuarioResponse {
    private final Long id;
    private final String nome;
    private final String email;
    private final Set<Role> roles;

    public UsuarioResponse(Long id, String nome, String email, Set<Role> roles) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public Set<Role> getRoles() { return roles; }
}
