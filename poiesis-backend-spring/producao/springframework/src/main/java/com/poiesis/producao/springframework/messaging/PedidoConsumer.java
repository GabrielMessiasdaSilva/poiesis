package com.poiesis.producao.springframework.messaging;

import com.poiesis.producao.domain.service.ProducaoService;
import com.poiesis.producao.springframework.config.RabbitMQConfig;
import com.poiesis.producao.springframework.messaging.event.PedidoCriadoEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class PedidoConsumer {

    private final ProducaoService producaoService;

    public PedidoConsumer(ProducaoService producaoService) {
        this.producaoService = producaoService;
    }

    // O evento inicia a produção de forma assíncrona, sem uma chamada HTTP do pedido.
    @RabbitListener(queues = RabbitMQConfig.PEDIDO_CRIADO_QUEUE)
    public void receberPedidoCriado(PedidoCriadoEvent event) {
        producaoService.iniciarProducao(event.pedidoId(), event.clienteEmail());
    }
}
