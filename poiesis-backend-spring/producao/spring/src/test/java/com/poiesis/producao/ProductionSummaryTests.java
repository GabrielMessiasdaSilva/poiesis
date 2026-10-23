package com.poiesis.producao;

import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.controller.ProducaoController;
import com.poiesis.producao.repository.*;
import com.poiesis.producao.repository.jpa.*;
import com.poiesis.producao.repository.orm.OrdemProducaoOrmJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ProducaoApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:production_test", "spring.rabbitmq.listener.simple.auto-startup=false"
})
class ProductionSummaryTests {
    @Autowired ProducaoController controller;
    @Autowired OrdemProducaoRepositoryWithJpa repository;
    @Test void calculatesLiveStatusCounts() {
        repository.deleteAll();
        assertTrue(controller.resumo().values().stream().allMatch(n -> n == 0));
        var order = repository.save(new OrdemProducaoOrmJpa(null, 1L, "test", StatusProducao.PENDENTE, null, null));
        repository.save(new OrdemProducaoOrmJpa(null, 2L, "test", StatusProducao.CANCELADO, null, null));
        assertEquals(1L, controller.resumo().get("emPendente"));
        order.setStatus(StatusProducao.EM_CORTE); repository.save(order);
        assertEquals(0L, controller.resumo().get("emPendente"));
        assertEquals(1L, controller.resumo().get("emCorte"));
    }
}
