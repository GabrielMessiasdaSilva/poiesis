package com.poiesis.login.springframework;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:login_context_test")
class SpringframeworkApplicationTests {

    @Test
    void contextLoads() {
    }

}
