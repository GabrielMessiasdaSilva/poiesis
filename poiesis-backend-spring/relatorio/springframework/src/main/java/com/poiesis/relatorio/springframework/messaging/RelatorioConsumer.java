package com.poiesis.relatorio.springframework.messaging;

import com.poiesis.relatorio.springframework.config.RabbitMQConfig;
import com.poiesis.relatorio.springframework.messaging.event.PedidoCriadoEvent;
import com.poiesis.relatorio.springframework.repository.SpringDataRelatorioRepository;
import com.poiesis.relatorio.springframework.repository.entity.RelatorioConsolidadoEntity;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class RelatorioConsumer {

    private final SpringDataRelatorioRepository repository;

    public RelatorioConsumer(SpringDataRelatorioRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.PEDIDO_CRIADO_QUEUE)
    // Mantém as operações de banco deste processamento na mesma transação.
    @Transactional
    public void processarEventoPedidoCriado(PedidoCriadoEvent event) {
        // Eventos inválidos são rejeitados sem voltar à fila para nova tentativa.
        if (event.pedidoId() == null) {
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Evento de pedido sem identificador válido.");
        }
        Long pedidoId = event.pedidoId();
        // Ignora pedidos já registrados; a restrição única no banco cobre inserções concorrentes.
        if (repository.existsByPedidoId(pedidoId)) {
            return;
        }
        // O valor precisa ser válido para não distorcer o faturamento.
        if (event.valorTotal() == null || event.valorTotal().signum() < 0) {
            throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Evento sem valor válido.");
        }
        var valorTotal = event.valorTotal();

        if (event.dataCriacao() == null) throw new org.springframework.amqp.AmqpRejectAndDontRequeueException("Evento sem data de criação do pedido.");
        LocalDate dataPedido = event.dataCriacao().toLocalDate();

        // Cada pedido gera uma entrada na data original da venda, mesmo se o evento chegar depois.
        RelatorioConsolidadoEntity registro = new RelatorioConsolidadoEntity(dataPedido, 1L, valorTotal, pedidoId);
        repository.save(registro);
    }
}
