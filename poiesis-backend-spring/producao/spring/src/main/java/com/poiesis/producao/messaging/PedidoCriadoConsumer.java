package com.poiesis.producao.messaging;

import com.poiesis.producao.service.ProducaoService;
import com.poiesis.producao.messaging.event.PedidoCriadoEvent;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCriadoConsumer {

    private final ProducaoService producaoService;

    public PedidoCriadoConsumer(ProducaoService producaoService) {
        this.producaoService = producaoService;
    }

    // O evento inicia a produção de forma assíncrona, sem uma chamada HTTP do pedido.
    @RabbitListener(queues = "#{pedidoCriadoQueue.name}")
    public void receberPedidoCriado(PedidoCriadoEvent event) {
        try {
            if (event == null) throw new IllegalArgumentException("Evento de pedido obrigatório.");
            producaoService.iniciarProducao(event.pedidoId(), event.clienteEmail());
        } catch (IllegalArgumentException e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
