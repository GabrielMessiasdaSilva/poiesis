package com.poiesis.produto.config;

import com.poiesis.produto.repository.ProdutoRepository;
import com.poiesis.produto.service.CatalogoService;
import com.poiesis.produto.repository.CustomizacaoRepository;
import com.poiesis.produto.service.CustomizacaoService;
import com.poiesis.produto.service.OpcaoCustomizacaoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CatalogoService catalogoService(ProdutoRepository produtoRepository) {
        return new CatalogoService(produtoRepository);
    }

    @Bean
    public OpcaoCustomizacaoService opcaoCustomizacaoService(CustomizacaoRepository repository) {
        return new OpcaoCustomizacaoService(repository);
    }

    @Bean
    public CustomizacaoService customizacaoService(OpcaoCustomizacaoService opcoes, CatalogoService produtos) {
        return new CustomizacaoService(opcoes, produtos);
    }
}
