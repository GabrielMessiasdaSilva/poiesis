package com.poiesis.pedido.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RabbitMQConfig {

    private final String exchangeName;

    public RabbitMQConfig(Environment environment) {
        this.exchangeName = environment.getRequiredProperty("application.rabbitmq.exchange");
    }

    @Bean
    public DirectExchange pedidoExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public JacksonJsonMessageConverter jsonMessageConverter() {
        // Converte os eventos Java para JSON e vice-versa nas mensagens AMQP.
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue producaoAtualizadaQueue(@Value("${application.rabbitmq.producao-status-queue:pedido.producao.atualizada.queue}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding producaoAtualizadaBinding(Queue producaoAtualizadaQueue, DirectExchange pedidoExchange,
            @Value("${application.rabbitmq.producao-routing-key:producao.atualizada}") String routingKey) {
        return BindingBuilder.bind(producaoAtualizadaQueue).to(pedidoExchange).with(routingKey);
    }
}
