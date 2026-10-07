package com.poiesis.pedido.springframework;

import com.poiesis.pedido.domain.entity.*;
import com.poiesis.pedido.domain.port.*;
import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;
import com.poiesis.pedido.domain.usecase.CriarPedidoUseCase;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomizacaoPedidoTests {
    private final PedidoRepositoryPort repository = mock(PedidoRepositoryPort.class);
    private final NotificacaoEventPort events = mock(NotificacaoEventPort.class);
    private final CustomizacaoServicePort options = mock(CustomizacaoServicePort.class);
    private final CatalogoServicePort catalog = id -> new CatalogoServicePort.Produto(id, "Camiseta", new BigDecimal("49.90"));
    private final CriarPedidoUseCase useCase = new CriarPedidoUseCase(repository, catalog, events, options);

    private ItemPedido item(List<Long> ids) {
        var item = new ItemPedido(1L, "Nome adulterado", 2, BigDecimal.ZERO);
        item.setCustomizacaoIds(ids);
        return item;
    }

    @Test void calculaAdicionalPorUnidadeESalvaCopiaDaOpcao() {
        var cor = new CustomizacaoEscolhida(7L, "COR", "Azul", new BigDecimal("5.00"));
        when(options.listarAtivas(1L)).thenReturn(List.of(cor));
        when(repository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        var order = useCase.executar("user@test.com", List.of(item(List.of(7L))));
        assertEquals(new BigDecimal("109.80"), order.getValorTotal());
        assertEquals("Camiseta", order.getItens().getFirst().getNomeProduto());
        assertEquals(List.of(cor), order.getItens().getFirst().getCustomizacoes());
        verify(events).notificarPedidoCriado(order);
    }

    @Test void rejeitaOpcaoInativaOuDeOutroProdutoAntesDeSalvar() {
        when(options.listarAtivas(1L)).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> useCase.executar("user@test.com", List.of(item(List.of(9L)))));
        verifyNoInteractions(repository, events);
    }

    @Test void rejeitaIdsDuplicadosEDuasOpcoesDoMesmoTipo() {
        assertThrows(IllegalArgumentException.class, () -> useCase.executar("user@test.com", List.of(item(List.of(7L, 7L)))));
        when(options.listarAtivas(1L)).thenReturn(List.of(
                new CustomizacaoEscolhida(7L, "COR", "Azul", BigDecimal.ONE),
                new CustomizacaoEscolhida(8L, "cor", "Verde", BigDecimal.ONE)));
        assertThrows(IllegalArgumentException.class, () -> useCase.executar("user@test.com", List.of(item(List.of(7L, 8L)))));
        verifyNoInteractions(repository, events);
    }

    @Test void pedidoSemCustomizacaoNaoDependeDoServicoDeOpcoes() {
        when(repository.salvar(any())).thenAnswer(i -> i.getArgument(0));
        var order = useCase.executar("user@test.com", List.of(item(List.of())));
        assertEquals(new BigDecimal("99.80"), order.getValorTotal());
        assertTrue(order.getItens().getFirst().getCustomizacoes().isEmpty());
        verifyNoInteractions(options);
    }
}
