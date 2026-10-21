package com.poiesis.pedido;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = PedidoApplication.class, properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class PedidoApplicationTests {

    @Test
    void contextLoads() {
    }

}
