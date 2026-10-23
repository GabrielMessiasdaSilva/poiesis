package com.poiesis.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.entity.StatusPedido;
import com.poiesis.pedido.messaging.ProducaoAtualizadaConsumer;
import com.poiesis.pedido.messaging.ProducaoAtualizadaEvent;
import com.poiesis.pedido.repository.PedidoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ProducaoAtualizadaConsumerTests {
    @Test
    void traduzEtapasDeProducaoParaStatusVisiveisNoPedido() {
        assertStatus("EM_COSTURA", StatusPedido.EM_PROCESSAMENTO);
        assertStatus("ENTREGUE", StatusPedido.FINALIZADO);
        assertStatus("CANCELADO", StatusPedido.CANCELADO);
    }

    private void assertStatus(String etapa, StatusPedido esperado) {
        var repository = new PedidoRepositoryFake();
        new ProducaoAtualizadaConsumer(repository).receberAtualizacao(evento(etapa));
        assertEquals(esperado, repository.status);
    }

    private ProducaoAtualizadaEvent evento(String status) {
        return new ProducaoAtualizadaEvent(42L, "cliente@poiesis.test", status, LocalDateTime.now());
    }

    private static final class PedidoRepositoryFake implements PedidoRepository {
        private StatusPedido status;

        @Override public Pedido salvar(Pedido pedido) { throw new UnsupportedOperationException(); }
        @Override public Optional<Pedido> buscarPorId(Long id) { return Optional.empty(); }
        @Override public List<Pedido> listarTodos() { return List.of(); }
        @Override public List<Pedido> listarPorClienteEmail(String email) { return List.of(); }
        @Override public void atualizarStatus(Long id, String email, StatusPedido novoStatus) { status = novoStatus; }
    }
}
