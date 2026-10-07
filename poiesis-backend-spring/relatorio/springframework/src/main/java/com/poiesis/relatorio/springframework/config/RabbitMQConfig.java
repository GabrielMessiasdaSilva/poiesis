package com.poiesis.relatorio.springframework.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String PEDIDO_CRIADO_QUEUE = "relatorio.pedido.criado.queue";
    public static final String PEDIDO_EXCHANGE = "pedido.eventos";
    public static final String PEDIDO_ROUTING_KEY = "pedido.criado";

    @Bean
    public DirectExchange pedidoExchange() { return new DirectExchange(PEDIDO_EXCHANGE); }

    @Bean
    public Queue pedidoCriadoQueue() {
        return new Queue(PEDIDO_CRIADO_QUEUE, true);
    }

    @Bean
    public Binding pedidoBinding(Queue pedidoCriadoQueue, DirectExchange pedidoExchange) {
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidoExchange).with(PEDIDO_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
