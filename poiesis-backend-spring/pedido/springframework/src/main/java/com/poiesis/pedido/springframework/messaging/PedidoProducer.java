package com.poiesis.pedido.springframework.messaging;

import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.port.NotificacaoEventPort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PedidoProducer implements NotificacaoEventPort {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public PedidoProducer(RabbitTemplate rabbitTemplate, Environment environment) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = environment.getRequiredProperty("application.rabbitmq.exchange");
        this.routingKey = environment.getRequiredProperty("application.rabbitmq.routing-key");
    }

    @Override
    public void notificarPedidoCriado(Pedido pedido) {
        PedidoCriadoEvent event = new PedidoCriadoEvent(pedido.getId(), pedido.getClienteEmail(), pedido.getValorTotal(), pedido.getDataCriacao());
        // Publica no exchange; o RabbitMQ encaminha o evento às filas pela routing key.
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }
}
