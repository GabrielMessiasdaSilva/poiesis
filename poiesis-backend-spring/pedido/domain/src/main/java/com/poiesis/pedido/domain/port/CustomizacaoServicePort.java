package com.poiesis.pedido.domain.port;

import com.poiesis.pedido.domain.entity.CustomizacaoEscolhida;
import java.util.List;

public interface CustomizacaoServicePort {
    List<CustomizacaoEscolhida> listarAtivas(Long produtoId);
}
