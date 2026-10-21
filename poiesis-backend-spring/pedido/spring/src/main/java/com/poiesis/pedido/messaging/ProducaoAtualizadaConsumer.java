package com.poiesis.pedido.messaging;

import com.poiesis.pedido.entity.StatusPedido;
import com.poiesis.pedido.repository.PedidoRepository;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ProducaoAtualizadaConsumer {
    private final PedidoRepository pedidos;

    public ProducaoAtualizadaConsumer(PedidoRepository pedidos) {
        this.pedidos = pedidos;
    }

    @RabbitListener(queues = "#{producaoAtualizadaQueue.name}")
    public void receberAtualizacao(ProducaoAtualizadaEvent event) {
        try {
            if (event == null || event.pedidoId() == null || event.clienteEmail() == null || event.status() == null) {
                throw new IllegalArgumentException("Evento de atualização de produção inválido.");
            }
            StatusPedido statusPedido = switch (event.status()) {
                case "CANCELADO" -> StatusPedido.CANCELADO;
                case "FINALIZADO", "ENTREGUE", "CONCLUIDO" -> StatusPedido.FINALIZADO;
                case "PENDENTE", "APROVADO", "EM_CORTE", "EM_COSTURA", "EM_PERSONALIZACAO", "ACABAMENTO" -> StatusPedido.EM_PROCESSAMENTO;
                default -> throw new IllegalArgumentException("Status de produção desconhecido: " + event.status());
            };
            pedidos.atualizarStatus(event.pedidoId(), event.clienteEmail(), statusPedido);
        } catch (IllegalArgumentException error) {
            throw new AmqpRejectAndDontRequeueException(error.getMessage(), error);
        }
    }
}
