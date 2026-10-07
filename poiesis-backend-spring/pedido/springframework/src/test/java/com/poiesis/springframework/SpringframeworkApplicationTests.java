package com.poiesis.springframework;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = com.poiesis.pedido.springframework.SpringframeworkApplication.class, properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class SpringframeworkApplicationTests {

    @Test
    void contextLoads() {
    }

}
