package com.poiesis.pedido;

import com.poiesis.pedido.integration.CatalogoIntegration;
import com.poiesis.pedido.integration.CustomizacaoIntegration;
import com.poiesis.pedido.messaging.PedidoPublisher;
import com.poiesis.pedido.entity.*;
import com.poiesis.pedido.repository.PedidoRepository;
import com.poiesis.pedido.service.PedidoService;
import com.poiesis.pedido.service.SelecionarCustomizacoesService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomizacaoPedidoTests {
    private final PedidoRepository repository = mock(PedidoRepository.class);
    private final PedidoPublisher events = mock(PedidoPublisher.class);
    private final CustomizacaoIntegration options = mock(CustomizacaoIntegration.class);
    private final CatalogoIntegration catalog = id -> new CatalogoIntegration.Produto(id, "Camiseta", new BigDecimal("49.90"));
    private final PedidoService service = new PedidoService(repository, catalog, events, new SelecionarCustomizacoesService(options));

    private ItemPedido item(List<Long> ids) {
        var item = new ItemPedido(1L, "Nome adulterado", 2, BigDecimal.ZERO);
        item.setCustomizacaoIds(ids);
        return item;
    }

    @Test void calculaAdicionalPorUnidadeESalvaCopiaDaOpcao() {
        var cor = new CustomizacaoEscolhida(7L, "COR", "Azul", new BigDecimal("5.00"));
        when(options.listarAtivas(1L)).thenReturn(List.of(cor));
        when(repository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        var order = service.criarPedido("user@test.com", List.of(item(List.of(7L))));
        assertEquals(new BigDecimal("109.80"), order.getValorTotal());
        assertEquals("Camiseta", order.getItens().getFirst().getNomeProduto());
        assertEquals(List.of(cor), order.getItens().getFirst().getCustomizacoes());
        verify(events).notificarPedidoCriado(order);
    }

    @Test void rejeitaOpcaoInativaOuDeOutroProdutoAntesDeSalvar() {
        when(options.listarAtivas(1L)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.criarPedido("user@test.com", List.of(item(List.of(9L)))));
        verifyNoInteractions(repository, events);
    }

    @Test void rejeitaIdsDuplicadosEDuasOpcoesDoMesmoTipo() {
        assertThrows(IllegalArgumentException.class, () -> service.criarPedido("user@test.com", List.of(item(List.of(7L, 7L)))));
        when(options.listarAtivas(1L)).thenReturn(List.of(
                new CustomizacaoEscolhida(7L, "COR", "Azul", BigDecimal.ONE),
                new CustomizacaoEscolhida(8L, "cor", "Verde", BigDecimal.ONE)));
        assertThrows(IllegalArgumentException.class, () -> service.criarPedido("user@test.com", List.of(item(List.of(7L, 8L)))));
        verifyNoInteractions(repository, events);
    }

    @Test void pedidoSemCustomizacaoNaoDependeDoServicoDeOpcoes() {
        when(repository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        var order = service.criarPedido("user@test.com", List.of(item(List.of())));
        assertEquals(new BigDecimal("99.80"), order.getValorTotal());
        assertTrue(order.getItens().getFirst().getCustomizacoes().isEmpty());
        verifyNoInteractions(options);
    }
}
