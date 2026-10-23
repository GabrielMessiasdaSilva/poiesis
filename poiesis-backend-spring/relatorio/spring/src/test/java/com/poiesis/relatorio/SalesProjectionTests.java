package com.poiesis.relatorio;

import com.poiesis.relatorio.messaging.PedidoCriadoConsumer;
import com.poiesis.relatorio.messaging.event.PedidoCriadoEvent;
import com.poiesis.relatorio.repository.*;
import com.poiesis.relatorio.repository.jpa.*;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = RelatorioApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:sales_test", "spring.rabbitmq.listener.simple.auto-startup=false",
    "application.rabbitmq.exchange=teste.pedidos",
    "application.rabbitmq.routing-key=teste.criado",
    "application.rabbitmq.queue=teste.relatorio"
})
class SalesProjectionTests {
    @Autowired PedidoCriadoConsumer consumer;
    @Autowired VendasRepositoryImpl adapter;
    @Autowired RelatorioRepositoryWithJpa repository;
    @Test void usesOrderDateDeduplicatesAndIncludesBothBoundaries() {
        repository.deleteAll();
        var first = new PedidoCriadoEvent(901L, "a@test.com", new BigDecimal("20.50"), LocalDateTime.parse("2026-10-01T00:00:00"));
        consumer.processarEventoPedidoCriado(first);
        consumer.processarEventoPedidoCriado(first);
        consumer.processarEventoPedidoCriado(new PedidoCriadoEvent(902L, "a@test.com", new BigDecimal("30.50"), LocalDateTime.parse("2026-10-31T23:59:59")));
        consumer.processarEventoPedidoCriado(new PedidoCriadoEvent(903L, "a@test.com", new BigDecimal("999"), LocalDateTime.parse("2026-11-01T00:00:00")));
        var metrics = adapter.obterMetricasVendas(LocalDate.parse("2026-10-01"), LocalDate.parse("2026-10-31"));
        assertEquals(2L, metrics.totalPedidos());
        assertEquals(0, new BigDecimal("51.00").compareTo(metrics.faturamentoTotal()));
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.processarEventoPedidoCriado(new PedidoCriadoEvent(904L, "a", BigDecimal.ONE, null)));
    }

    @Autowired DirectExchange exchange;
    @Autowired Queue queue;
    @Autowired Binding binding;
    @Autowired RabbitListenerEndpointRegistry listeners;

    @Test void consumidorUsaAFilaEOContratoConfigurados() {
        assertEquals("teste.pedidos", exchange.getName());
        assertEquals("teste.relatorio", queue.getName());
        assertEquals(exchange.getName(), binding.getExchange());
        assertEquals(queue.getName(), binding.getDestination());
        assertEquals("teste.criado", binding.getRoutingKey());
        var container = (SimpleMessageListenerContainer) listeners.getListenerContainers().iterator().next();
        assertArrayEquals(new String[]{queue.getName()}, container.getQueueNames());
    }

    @Test void rejeitaMensagensSemIdentificadorValidoSemPersistir() {
        long antes = repository.count();
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.processarEventoPedidoCriado(null));
        for (Long id : new Long[]{null, 0L, -1L}) {
            assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.processarEventoPedidoCriado(
                    new PedidoCriadoEvent(id, "teste@example.com", BigDecimal.TEN, LocalDateTime.now())));
        }
        assertEquals(antes, repository.count());
    }
}
