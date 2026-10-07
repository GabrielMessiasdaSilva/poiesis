package com.poiesis.customizacao.springframework.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalogo-service", url = "${application.config.catalogo-url:http://localhost:8082}")
public interface CatalogoFeignClient {

    @GetMapping("/v1/produtos/{id}")
    Object buscarProdutoPorId(@PathVariable("id") Long id);
}
