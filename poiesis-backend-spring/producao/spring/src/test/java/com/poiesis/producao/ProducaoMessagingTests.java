package com.poiesis.producao;

import com.poiesis.producao.messaging.PedidoCriadoConsumer;
import com.poiesis.producao.messaging.event.PedidoCriadoEvent;
import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.repository.OrdemProducaoRepository;
import com.poiesis.producao.repository.jpa.OrdemProducaoRepositoryWithJpa;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:production_messaging_test",
    "spring.rabbitmq.listener.simple.auto-startup=false",
    "application.rabbitmq.exchange=teste.pedidos",
    "application.rabbitmq.routing-key=teste.criado",
    "application.rabbitmq.queue=teste.producao"
})
class ProducaoMessagingTests {
    @Autowired PedidoCriadoConsumer consumer;
    @Autowired OrdemProducaoRepositoryWithJpa repository;
    @Autowired OrdemProducaoRepository ordemRepository;
    @Autowired DirectExchange exchange;
    @Autowired Queue queue;
    @Autowired Binding binding;
    @Autowired RabbitListenerEndpointRegistry listeners;

    @Test void consumidorUsaAFilaEOContratoConfigurados() {
        assertEquals("teste.pedidos", exchange.getName());
        assertEquals("teste.producao", queue.getName());
        assertEquals(exchange.getName(), binding.getExchange());
        assertEquals(queue.getName(), binding.getDestination());
        assertEquals("teste.criado", binding.getRoutingKey());
        var container = (SimpleMessageListenerContainer) listeners.getListenerContainers().iterator().next();
        assertArrayEquals(new String[]{queue.getName()}, container.getQueueNames());
    }

    @Test void entregaRepetidaNaoDuplicaOrdem() {
        var event = new PedidoCriadoEvent(5001L, "teste@example.com", BigDecimal.TEN, LocalDateTime.now());
        consumer.receberPedidoCriado(event);
        long id = repository.findByPedidoId(5001L).orElseThrow().getId();
        consumer.receberPedidoCriado(event);
        assertEquals(id, repository.findByPedidoId(5001L).orElseThrow().getId());
        assertEquals(1, repository.findAll().stream().filter(o -> o.getPedidoId().equals(5001L)).count());
    }

    @Test void mensagemInvalidaEhRejeitadaSemReenfileirar() {
        long antes = repository.count();
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.receberPedidoCriado(null));
        for (Long id : new Long[]{null, 0L, -1L}) {
            assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.receberPedidoCriado(
                    new PedidoCriadoEvent(id, "teste@example.com", BigDecimal.TEN, LocalDateTime.now())));
        }
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> consumer.receberPedidoCriado(
                new PedidoCriadoEvent(5002L, " ", BigDecimal.TEN, LocalDateTime.now())));
        assertEquals(antes, repository.count());
    }

    @Test void historicoDeStatusPermanecePersistidoEntreLeituras() {
        var order = ordemRepository.salvar(new OrdemProducao(5003L, "historico@example.com"));
        order.atualizarStatus(StatusProducao.APROVADO);
        ordemRepository.salvar(order);

        var recuperada = ordemRepository.buscarPorPedidoId(5003L).orElseThrow();
        assertEquals(List.of(StatusProducao.PENDENTE, StatusProducao.APROVADO),
                recuperada.getHistorico().stream().map(item -> item.status()).toList());
    }
}
