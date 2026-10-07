package com.poiesis.login.springframework.config;

import com.poiesis.login.domain.entity.Role;
import com.poiesis.login.springframework.repository.SpringDataUsuarioRepository;
import com.poiesis.login.springframework.repository.entity.UsuarioEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.core.env.Environment;

import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final SpringDataUsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(SpringDataUsuarioRepository repository, PasswordEncoder passwordEncoder,
                           Environment environment) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = environment.getRequiredProperty("application.bootstrap-admin.email");
        this.adminPassword = environment.getRequiredProperty("application.bootstrap-admin.password");
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByEmail(adminEmail)) {
            UsuarioEntity admin = new UsuarioEntity();
            admin.setNome("Administrador");
            admin.setEmail(adminEmail);
            admin.setSenha(passwordEncoder.encode(adminPassword));
            admin.setAtivo(true);
            admin.setRoles(Set.of(Role.ADMIN, Role.USER));

            repository.save(admin);
            System.out.println(">>> Usuário ADMIN de bootstrap criado: " + adminEmail);
        }
    }
}
