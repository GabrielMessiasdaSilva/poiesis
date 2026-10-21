package com.poiesis.pedido.service;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import com.poiesis.pedido.integration.CustomizacaoIntegration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/** Valida a seleção e resolve as opções ativas, sem persistir ou publicar pedidos. */
public class SelecionarCustomizacoesService {
    private final CustomizacaoIntegration customizacao;

    public SelecionarCustomizacoesService(CustomizacaoIntegration customizacao) { this.customizacao = customizacao; }

    public List<CustomizacaoEscolhida> selecionar(Long produtoId, List<Long> ids) {
        if (ids.size() > 20 || ids.stream().anyMatch(id -> id == null || id <= 0)
                || new HashSet<>(ids).size() != ids.size()) {
            throw new IllegalArgumentException("Identificadores de customização inválidos ou duplicados.");
        }
        var escolhidas = new ArrayList<CustomizacaoEscolhida>();
        var tipos = new HashSet<String>();
        if (!ids.isEmpty()) {
            var ativas = customizacao.listarAtivas(produtoId);
            for (Long id : ids) {
                var opcao = ativas.stream().filter(o -> id.equals(o.id())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Customização indisponível para este produto: " + id));
                if (opcao.precoAdicional() == null || opcao.precoAdicional().signum() < 0
                        || opcao.tipo() == null || !tipos.add(opcao.tipo().trim().toUpperCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Selecione uma opção válida por tipo de customização.");
                }
                escolhidas.add(opcao);
            }
        }
        return escolhidas;
    }
}
