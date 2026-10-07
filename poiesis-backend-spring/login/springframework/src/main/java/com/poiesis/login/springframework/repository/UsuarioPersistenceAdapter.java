package com.poiesis.login.springframework.repository;

import com.poiesis.login.domain.entity.Usuario;
import com.poiesis.login.domain.repository.UsuarioRepositoryPort;
import com.poiesis.login.springframework.controller.adapter.UsuarioMapper;
import com.poiesis.login.springframework.repository.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final SpringDataUsuarioRepository springDataRepository;

    public UsuarioPersistenceAdapter(SpringDataUsuarioRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioEntity entity = UsuarioMapper.toEntity(usuario);
        UsuarioEntity saved = springDataRepository.save(entity);
        return UsuarioMapper.toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return springDataRepository.findByEmail(email).map(UsuarioMapper::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return springDataRepository.findById(id).map(UsuarioMapper::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }
}