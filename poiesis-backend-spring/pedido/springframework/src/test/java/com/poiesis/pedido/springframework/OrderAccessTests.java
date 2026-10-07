package com.poiesis.pedido.springframework;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.poiesis.pedido.springframework.controller.PedidoController;
import com.poiesis.pedido.springframework.repository.*;
import com.poiesis.pedido.springframework.repository.entity.*;
import com.poiesis.pedido.domain.entity.StatusPedido;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:orders_test", "spring.rabbitmq.listener.simple.auto-startup=false"})
class OrderAccessTests {
    @Autowired PedidoController controller;
    @Autowired SpringDataPedidoRepository repository;
    Jwt as(String email, String role) {
        var jwt = Jwt.withTokenValue("test").header("alg", "HS256").subject(email).claim("roles", List.of(role)).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwt, "unused", List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        return jwt;
    }
    @Test void listUsesDatabaseAndHidesOrdersOfOtherCustomers() {
        repository.deleteAll();
        var mine = repository.save(new PedidoEntity(null, "a@test.com", List.of(new ItemPedidoEntity(1L,"Produto",1,BigDecimal.TEN)), BigDecimal.TEN, StatusPedido.CRIADO, LocalDateTime.now()));
        var other = repository.save(new PedidoEntity(null, "b@test.com", List.of(), BigDecimal.ONE, StatusPedido.CRIADO, LocalDateTime.now()));
        try {
            var user = as("a@test.com", "USER");
            var orders = controller.listarPedidos(user);
            assertEquals(1, orders.size());
            assertEquals(mine.getId(), orders.getFirst().getId());
            assertEquals(200, controller.buscarPedido(mine.getId(), user).getStatusCode().value());
            assertEquals(404, controller.buscarPedido(other.getId(), user).getStatusCode().value());
            assertEquals(2, controller.listarPedidos(as("admin@poiesis.com", "ADMIN")).size());
        } finally { SecurityContextHolder.clearContext(); }
    }
}
