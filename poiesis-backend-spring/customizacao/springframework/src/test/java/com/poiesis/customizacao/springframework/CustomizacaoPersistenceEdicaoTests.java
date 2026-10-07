package com.poiesis.customizacao.springframework;

import com.poiesis.customizacao.domain.entity.OpcaoCustomizacao;
import com.poiesis.customizacao.domain.service.CustomizacaoDomainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CustomizacaoPersistenceEdicaoTests {
    @Autowired CustomizacaoDomainService service;

    @Test
    void edicaoInvalidaNaoAlteraRegistro() {
        OpcaoCustomizacao criada = service.criarOpcao(new OpcaoCustomizacao(null, 986L, "COR", "Azul", BigDecimal.ZERO, true));
        assertThrows(IllegalArgumentException.class, () -> service.atualizarOpcao(criada.id(), new OpcaoCustomizacao(null, 986L, "COR", "", BigDecimal.ZERO, true)));
        assertEquals("Azul", service.listarPorProduto(986L).getFirst().nome());
        assertThrows(IllegalArgumentException.class, () -> service.atualizarOpcao(Long.MAX_VALUE, new OpcaoCustomizacao(null, 986L, "COR", "Azul", BigDecimal.ZERO, true)));
    }

    @Test
    void editarPersistidoNaoCriaDuplicataEExclusaoOcultaOpcao() {
        OpcaoCustomizacao criada = service.criarOpcao(new OpcaoCustomizacao(null, 987L, "COR", "Azul", BigDecimal.ZERO, true));
        OpcaoCustomizacao atualizada = service.atualizarOpcao(criada.id(),
                new OpcaoCustomizacao(null, 987L, "COR", "Verde", BigDecimal.TEN, true));
        assertEquals(criada.id(), atualizada.id());
        assertEquals(1, service.listarPorProduto(987L).size());
        assertEquals("Verde", service.listarPorProduto(987L).getFirst().nome());
        assertTrue(service.inativarOpcao(criada.id()));
        assertTrue(service.listarPorProduto(987L).isEmpty());
    }
}
