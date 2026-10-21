package com.poiesis.producao;

import com.poiesis.producao.entity.StatusProducao;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatusProducaoTests {
    @Test void permiteSomenteAsTransicoesDeProducaoEsperadas() {
        assertTrue(StatusProducao.PENDENTE.podeTransicionarPara(StatusProducao.APROVADO));
        assertTrue(StatusProducao.APROVADO.podeTransicionarPara(StatusProducao.EM_CORTE));
        assertTrue(StatusProducao.EM_CORTE.podeTransicionarPara(StatusProducao.EM_COSTURA));
        assertTrue(StatusProducao.EM_COSTURA.podeTransicionarPara(StatusProducao.EM_PERSONALIZACAO));
        assertTrue(StatusProducao.EM_PERSONALIZACAO.podeTransicionarPara(StatusProducao.FINALIZADO));
        assertTrue(StatusProducao.FINALIZADO.podeTransicionarPara(StatusProducao.ENTREGUE));
        assertFalse(StatusProducao.PENDENTE.podeTransicionarPara(StatusProducao.EM_CORTE));
        assertFalse(StatusProducao.ENTREGUE.podeTransicionarPara(StatusProducao.CANCELADO));
    }
}