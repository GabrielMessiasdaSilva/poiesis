package com.poiesis.producao.messaging;

import com.poiesis.producao.entity.OrdemProducao;
import java.time.LocalDateTime;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProducaoPublisherImpl implements ProducaoPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public ProducaoPublisherImpl(RabbitTemplate rabbitTemplate,
            @Value("${application.rabbitmq.exchange:pedido.eventos}") String exchange,
            @Value("${application.rabbitmq.producao-routing-key:producao.atualizada}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void notificarAtualizacao(OrdemProducao ordem) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey,
                    new ProducaoAtualizadaEvent(ordem.getPedidoId(), ordem.getClienteEmail(), ordem.getStatus(), LocalDateTime.now()));
        } catch (AmqpException error) {
            // O status já foi gravado; o broker indisponível não pode bloquear a operação do administrador.
            org.slf4j.LoggerFactory.getLogger(getClass()).warn(
                    "Atualização de produção {} salva, mas não publicada no RabbitMQ.", ordem.getId(), error);
        }
    }
}