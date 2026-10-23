package com.poiesis.login;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:login_context_test")
class LoginApplicationTests {

    @Test
    void contextLoads() {
    }

}
