package com.poiesis.relatorio.messaging;

import com.poiesis.relatorio.service.RegistroVendaService;
import com.poiesis.relatorio.messaging.event.PedidoCriadoEvent;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoCriadoConsumer {
    private final RegistroVendaService registrarVenda;

    public PedidoCriadoConsumer(RegistroVendaService registrarVenda) { this.registrarVenda = registrarVenda; }

    @RabbitListener(queues = "#{pedidoCriadoQueue.name}")
    public void processarEventoPedidoCriado(PedidoCriadoEvent event) {
        try {
            if (event == null) throw new IllegalArgumentException("Evento de pedido obrigatório.");
            registrarVenda.registrar(event.pedidoId(), event.valorTotal(), event.dataCriacao());
        } catch (IllegalArgumentException e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
