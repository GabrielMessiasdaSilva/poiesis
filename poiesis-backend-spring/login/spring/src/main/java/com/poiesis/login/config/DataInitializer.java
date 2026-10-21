package com.poiesis.login.config;

import com.poiesis.login.entity.Role;
import com.poiesis.login.repository.jpa.UsuarioRepositoryWithJpa;
import com.poiesis.login.repository.orm.UsuarioOrmJpa;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositoryWithJpa repository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UsuarioRepositoryWithJpa repository, PasswordEncoder passwordEncoder,
                           Environment environment) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = environment.getRequiredProperty("application.bootstrap-admin.email");
        this.adminPassword = environment.getRequiredProperty("application.bootstrap-admin.password");
    }

    @Override
    public void run(String... args) {
        if (!repository.existsByEmail(adminEmail)) {
            UsuarioOrmJpa admin = new UsuarioOrmJpa();
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
