package com.poiesis.relatorio.springframework;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.poiesis.relatorio.springframework.messaging.RelatorioConsumer;
import com.poiesis.relatorio.springframework.messaging.event.PedidoCriadoEvent;
import com.poiesis.relatorio.springframework.repository.*;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(classes = com.poiesis.relatorio.SpringframeworkApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:sales_test", "spring.rabbitmq.listener.simple.auto-startup=false"
})
class SalesProjectionTests {
    @Autowired RelatorioConsumer consumer;
    @Autowired RelatorioPersistenceAdapter adapter;
    @Autowired SpringDataRelatorioRepository repository;
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
        assertThrows(org.springframework.amqp.AmqpRejectAndDontRequeueException.class, () -> consumer.processarEventoPedidoCriado(new PedidoCriadoEvent(904L, "a", BigDecimal.ONE, null)));
    }
}
