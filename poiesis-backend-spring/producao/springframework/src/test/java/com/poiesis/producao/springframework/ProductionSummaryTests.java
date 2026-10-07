package com.poiesis.producao.springframework;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.poiesis.producao.springframework.controller.ProducaoController;
import com.poiesis.producao.springframework.repository.*;
import com.poiesis.producao.springframework.repository.entity.OrdemProducaoEntity;
import com.poiesis.producao.domain.entity.StatusProducao;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes = com.poiesis.producao.SpringframeworkApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:production_test", "spring.rabbitmq.listener.simple.auto-startup=false"
})
class ProductionSummaryTests {
    @Autowired ProducaoController controller;
    @Autowired SpringDataOrdemProducaoRepository repository;
    void as(String role) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test", "unused", List.of(new SimpleGrantedAuthority("ROLE_" + role)))); }
    @Test void calculatesLiveStatusCountsAndRestrictsCommonUsers() {
        repository.deleteAll();
        try {
            as("ADMIN");
            assertTrue(controller.resumo().values().stream().allMatch(n -> n == 0));
            var order = repository.save(new OrdemProducaoEntity(null, 1L, "test", StatusProducao.PENDENTE, null, null));
            repository.save(new OrdemProducaoEntity(null, 2L, "test", StatusProducao.CANCELADO, null, null));
            assertEquals(1L, controller.resumo().get("emPendente"));
            order.setStatus(StatusProducao.EM_CORTE); repository.save(order);
            assertEquals(0L, controller.resumo().get("emPendente"));
            assertEquals(1L, controller.resumo().get("emCorte"));
            as("USER");
            assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> controller.resumo());
            assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> controller.listarTodas());
        } finally { SecurityContextHolder.clearContext(); }
    }
}
