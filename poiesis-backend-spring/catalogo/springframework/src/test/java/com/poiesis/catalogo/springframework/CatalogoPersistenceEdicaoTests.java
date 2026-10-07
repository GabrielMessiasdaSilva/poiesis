package com.poiesis.catalogo.springframework;

import com.poiesis.catalogo.domain.entity.Categoria;
import com.poiesis.catalogo.domain.entity.Produto;
import com.poiesis.catalogo.domain.service.CatalogoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CatalogoPersistenceEdicaoTests {
    @Autowired CatalogoService service;

    @Test
    void edicaoInvalidaNaoAlteraProdutoEIdInexistenteNaoCriaRegistro() {
        Produto criado = service.cadastrarProduto(new Produto("Teste", "", BigDecimal.TEN, Categoria.CAMISA));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarProduto(criado.getId(), new Produto("Inválido", "", BigDecimal.ZERO, Categoria.CAMISA)));
        assertEquals("Teste", service.buscarPorId(criado.getId()).getNome());
        assertThrows(IllegalArgumentException.class, () -> service.atualizarProduto(Long.MAX_VALUE, new Produto("Teste", "", BigDecimal.TEN, Categoria.CAMISA)));
    }

    @Test
    void edicaoMantemIdentidadeEExclusaoRetiraDoCatalogo() {
        Produto criado = service.cadastrarProduto(new Produto("Teste", "Descrição", BigDecimal.TEN, Categoria.CAMISA));
        Produto editado = service.atualizarProduto(criado.getId(), new Produto("Editado", "Nova descrição", new BigDecimal("25.50"), Categoria.CAMISETA));
        assertEquals(criado.getId(), editado.getId());
        assertEquals("Editado", service.buscarPorId(criado.getId()).getNome());
        service.inativarProduto(criado.getId());
        assertFalse(service.buscarPorId(criado.getId()).getAtivo());
        assertTrue(service.listarProdutosAtivos().stream().noneMatch(p -> p.getId().equals(criado.getId())));
    }
}
