package com.poiesis.login.entity;

import java.util.List;

public record UsuarioAutenticado(String email, List<String> roles) {}
