package com.poiesis.relatorio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = RelatorioApplication.class, properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class RelatorioApplicationTests {

    @Test
    void contextLoads() {
    }

}
