package com.poiesis.catalogo.springframework.config;

import com.poiesis.catalogo.domain.repository.ProdutoRepository;
import com.poiesis.catalogo.domain.service.CatalogoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CatalogoService catalogoService(ProdutoRepository produtoRepository) {
        return new CatalogoService(produtoRepository);
    }
}