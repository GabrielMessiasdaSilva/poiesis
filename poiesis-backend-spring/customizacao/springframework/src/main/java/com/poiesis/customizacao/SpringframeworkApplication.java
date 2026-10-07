package com.poiesis.customizacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.poiesis.customizacao.springframework.integration")
public class SpringframeworkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringframeworkApplication.class, args);
    }

}
