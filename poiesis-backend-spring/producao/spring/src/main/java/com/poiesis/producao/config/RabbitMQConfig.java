package com.poiesis.producao.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Produção e relatório têm filas próprias, para ambos receberem o mesmo evento.
    public static final String PEDIDO_CRIADO_QUEUE = "producao.pedido.criado.queue";

    @Bean
    public DirectExchange pedidoExchange(@Value("${application.rabbitmq.exchange:pedido.eventos}") String exchange) {
        return new DirectExchange(exchange);
    }

    @Bean
    public Queue pedidoCriadoQueue(@Value("${application.rabbitmq.queue:" + PEDIDO_CRIADO_QUEUE + "}") String queue) {
        // A fila é durável: sua definição permanece após reiniciar o broker.
        return new Queue(queue, true);
    }

    @Bean
    public Binding pedidoBinding(Queue pedidoCriadoQueue, DirectExchange pedidoExchange,
            @Value("${application.rabbitmq.routing-key:pedido.criado}") String routingKey) {
        // O exchange direto entrega o evento às filas vinculadas com esta routing key.
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidoExchange).with(routingKey);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        // Converte os eventos Java para JSON e vice-versa nas mensagens AMQP.
        return new JacksonJsonMessageConverter();
    }
}
