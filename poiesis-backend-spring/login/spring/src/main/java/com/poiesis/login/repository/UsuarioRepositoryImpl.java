package com.poiesis.login.repository;

import com.poiesis.login.entity.Usuario;
import com.poiesis.login.repository.UsuarioRepository;
import com.poiesis.login.repository.adapter.UsuarioRepositoryAdapter;
import com.poiesis.login.repository.jpa.UsuarioRepositoryWithJpa;
import com.poiesis.login.repository.orm.UsuarioOrmJpa;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioRepositoryWithJpa springDataRepository;

    public UsuarioRepositoryImpl(UsuarioRepositoryWithJpa springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        UsuarioOrmJpa entity = UsuarioRepositoryAdapter.toOrm(usuario);
        UsuarioOrmJpa saved = springDataRepository.save(entity);
        return UsuarioRepositoryAdapter.toDomain(saved);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return springDataRepository.findByEmail(email).map(UsuarioRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return springDataRepository.findById(id).map(UsuarioRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existePorEmail(String email) {
        return springDataRepository.existsByEmail(email);
    }
}