package com.poiesis.produto;

import com.poiesis.produto.entity.OpcaoCustomizacao;
import com.poiesis.produto.repository.CustomizacaoRepository;
import com.poiesis.produto.service.CustomizacaoService;
import com.poiesis.produto.service.CatalogoService;
import static org.mockito.Mockito.*;
import com.poiesis.produto.service.OpcaoCustomizacaoService;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomizacaoServiceTests {
    private final MemoryRepository repository = new MemoryRepository();
    private final OpcaoCustomizacaoService opcoes = new OpcaoCustomizacaoService(repository);

    private OpcaoCustomizacao opcao(String nome) {
        return new OpcaoCustomizacao(null, 1L, "COR", nome, BigDecimal.ONE, true);
    }

    @Test void validaProdutoAntesDeCriarOuAtualizar() {
        var criada = opcoes.criarOpcao(opcao("Azul"));
        var catalogo = mock(CatalogoService.class);
        when(catalogo.buscarPorId(1L)).thenThrow(new IllegalArgumentException("Produto não existe."));
        var service = new CustomizacaoService(opcoes, catalogo);
        assertThrows(IllegalArgumentException.class, () -> service.criarOpcao(opcao("Verde")));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarOpcao(criada.id(), opcao("Verde")));
        assertEquals(1, repository.dados.size());
        assertEquals("Azul", repository.dados.get(criada.id()).nome());
    }

    @Test void consultaCatalogoEPreservaIdentidadeNaEdicao() {
        var catalogo = mock(CatalogoService.class);
        var service = new CustomizacaoService(opcoes, catalogo);
        var criada = service.criarOpcao(opcao("Azul"));
        var atualizada = service.atualizarOpcao(criada.id(), opcao("Verde"));
        verify(catalogo, times(2)).buscarPorId(1L);
        assertEquals(criada.id(), atualizada.id());
        assertEquals("Verde", service.listarPorProduto(1L).getFirst().nome());
    }

    private static class MemoryRepository implements CustomizacaoRepository {
        final Map<Long, OpcaoCustomizacao> dados = new HashMap<>();
        @Override public OpcaoCustomizacao salvar(OpcaoCustomizacao opcao) {
            long id = opcao.id() == null ? dados.size() + 1L : opcao.id();
            var salva = new OpcaoCustomizacao(id, opcao.produtoId(), opcao.tipo(), opcao.nome(), opcao.precoAdicional(), opcao.ativo());
            dados.put(id, salva);
            return salva;
        }
        @Override public Optional<OpcaoCustomizacao> buscarPorId(Long id) { return Optional.ofNullable(dados.get(id)); }
        @Override public List<OpcaoCustomizacao> buscarPorProdutoId(Long id) {
            return dados.values().stream().filter(opcao -> opcao.produtoId().equals(id) && opcao.ativo()).toList();
        }
        @Override public boolean inativar(Long id) { return dados.remove(id) != null; }
    }
}
