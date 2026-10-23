package com.poiesis.producao.messaging;

import com.poiesis.producao.entity.OrdemProducao;

public interface ProducaoPublisher {
    void notificarAtualizacao(OrdemProducao ordem);
}