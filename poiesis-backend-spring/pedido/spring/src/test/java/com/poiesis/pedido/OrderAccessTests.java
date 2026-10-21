package com.poiesis.pedido;

import com.poiesis.pedido.entity.StatusPedido;
import com.poiesis.pedido.controller.PedidoController;
import com.poiesis.pedido.controller.dto.request.PedidoRequest;
import com.poiesis.pedido.repository.*;
import com.poiesis.pedido.repository.jpa.*;
import com.poiesis.pedido.repository.orm.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"spring.datasource.url=jdbc:h2:mem:orders_test", "spring.rabbitmq.listener.simple.auto-startup=false"})
class OrderAccessTests {
    @Autowired PedidoController controller;
    @Autowired PedidoRepositoryWithJpa repository;
    @Test void listUsesDatabaseAndHidesOrdersOfOtherCustomers() {
        repository.deleteAll();
        var mine = repository.save(new PedidoOrmJpa(null, "a@test.com", List.of(new ItemPedidoOrmJpa(1L,"Produto",1,BigDecimal.TEN)), BigDecimal.TEN, StatusPedido.CRIADO, LocalDateTime.now()));
        var other = repository.save(new PedidoOrmJpa(null, "b@test.com", List.of(), BigDecimal.ONE, StatusPedido.CRIADO, LocalDateTime.now()));
        var orders = controller.listarPedidos("a@test.com", List.of("USER"));
        assertEquals(1, orders.size());
        assertEquals(mine.getId(), orders.getFirst().getId());
        assertEquals(200, controller.buscarPedido(mine.getId(), "a@test.com", List.of("USER")).getStatusCode().value());
        assertEquals(404, controller.buscarPedido(other.getId(), "a@test.com", List.of("USER")).getStatusCode().value());
        assertEquals(2, controller.listarPedidos("admin@poiesis.com", List.of("ADMIN")).size());
    }

    @Test void adminCannotCreateOrderEvenWhenAccountAlsoHasUserRole() {
        var response = controller.criarPedido(new PedidoRequest(), "admin@poiesis.com", List.of("USER", "ADMIN"));
        assertEquals(403, response.getStatusCode().value());
    }
}
