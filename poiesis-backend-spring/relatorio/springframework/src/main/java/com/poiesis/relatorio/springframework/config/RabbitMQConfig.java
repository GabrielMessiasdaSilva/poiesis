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

    // Produção e relatório têm filas próprias, para ambos receberem o mesmo evento.
    public static final String PEDIDO_CRIADO_QUEUE = "relatorio.pedido.criado.queue";
    public static final String PEDIDO_EXCHANGE = "pedido.eventos";
    public static final String PEDIDO_ROUTING_KEY = "pedido.criado";

    @Bean
    public DirectExchange pedidoExchange() { return new DirectExchange(PEDIDO_EXCHANGE); }

    @Bean
    public Queue pedidoCriadoQueue() {
        // A fila é durável: sua definição permanece após reiniciar o broker.
        return new Queue(PEDIDO_CRIADO_QUEUE, true);
    }

    @Bean
    public Binding pedidoBinding(Queue pedidoCriadoQueue, DirectExchange pedidoExchange) {
        // O exchange direto entrega o evento às filas vinculadas com esta routing key.
        return BindingBuilder.bind(pedidoCriadoQueue).to(pedidoExchange).with(PEDIDO_ROUTING_KEY);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        // Converte os eventos Java para JSON e vice-versa nas mensagens AMQP.
        return new JacksonJsonMessageConverter();
    }
}
