package com.poiesis.producao;

import com.poiesis.producao.config.RabbitMQConfig;
import com.poiesis.producao.messaging.PedidoCriadoConsumer;
import com.poiesis.producao.messaging.event.PedidoCriadoEvent;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import static org.junit.jupiter.api.Assertions.*;

class PedidoCriadoMessageCompatibilityTests {
    @Test void aceitaMensagensPublicadasAntesEDepoisDaReorganizacao() throws Exception {
        var converter = new RabbitMQConfig().jacksonJsonMessageConverter();
        var listenerType = PedidoCriadoConsumer.class.getMethod("receberPedidoCriado", PedidoCriadoEvent.class)
                .getGenericParameterTypes()[0];
        String json = "{\"pedidoId\":81,\"clienteEmail\":\"user@test.com\",\"valorTotal\":25.50,\"dataCriacao\":\"2026-10-01T10:00:00\"}";
        for (String type : List.of("com.poiesis.pedido.springframework.messaging.PedidoCriadoEvent",
                "com.poiesis.pedido.springframework.adapter.out.messaging.PedidoCriadoEvent",
                "com.poiesis.pedido.spring.messaging.PedidoCriadoEvent",
                "com.poiesis.pedido.messaging.PedidoCriadoEvent")) {
            var properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setHeader("__TypeId__", type);
            properties.setInferredArgumentType(listenerType);
            var event = assertInstanceOf(PedidoCriadoEvent.class,
                    converter.fromMessage(new Message(json.getBytes(StandardCharsets.UTF_8), properties)));
            assertEquals(81L, event.pedidoId());
            assertEquals("user@test.com", event.clienteEmail());
            assertEquals(0, new BigDecimal("25.50").compareTo(event.valorTotal()));
            assertEquals(LocalDateTime.parse("2026-10-01T10:00:00"), event.dataCriacao());
        }
    }
}
