package com.poiesis.login.domain.entity;

import java.util.HashSet;
import java.util.Set;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senha; // Armazena o hash/senha tratada
    private Boolean ativo;
    private Set<Role> roles = new HashSet<>();

    public Usuario() {}

    public Usuario(Long id, String nome, String email, String senha, Boolean ativo, Set<Role> roles) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.ativo = ativo != null ? ativo : true;
        if (roles != null) {
            this.roles = roles;
        }
    }

    // Regras de negócio do domínio
    public void adicionarRole(Role role) {
        this.roles.add(role);
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean eAdmin() {
        return this.roles != null && this.roles.contains(Role.ADMIN);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
}
