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
    @Transactional
    public void processarEventoPedidoCriado(PedidoCriadoEvent event) {
        if (event.pedidoId() == null) {
            throw new IllegalArgumentException("Evento de pedido sem identificador válido.");
        }
        Long pedidoId = event.pedidoId();
        if (repository.existsByPedidoId(pedidoId)) {
            return;
        }
        // Extrai o valor do pedido e registra o incremento diário nas métricas
        var valorTotal = event.valorTotal() == null ? java.math.BigDecimal.ZERO : event.valorTotal();

        LocalDate hoje = LocalDate.now();

        // Registra uma nova entrada de consolidação para o dia
        RelatorioConsolidadoEntity registro = new RelatorioConsolidadoEntity(hoje, 1L, valorTotal, pedidoId);
        repository.save(registro);
    }
}
