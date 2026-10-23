package com.poiesis.pedido.integration;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import java.util.List;

public interface CustomizacaoIntegration {
    List<CustomizacaoEscolhida> listarAtivas(Long produtoId);
}
