package com.poiesis.pedido.domain.usecase;

import com.poiesis.pedido.domain.entity.ItemPedido;
import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.domain.port.CatalogoServicePort;
import com.poiesis.pedido.domain.port.NotificacaoEventPort;
import com.poiesis.pedido.domain.repository.PedidoRepositoryPort;

import java.util.List;

public class CriarPedidoUseCase {
    private final PedidoRepositoryPort pedidoRepository;
    private final CatalogoServicePort catalogo;
    private final NotificacaoEventPort notificacao;
    private final com.poiesis.pedido.domain.port.CustomizacaoServicePort customizacao;

    public CriarPedidoUseCase(PedidoRepositoryPort pedidoRepository, CatalogoServicePort catalogo,
                              NotificacaoEventPort notificacao,
                              com.poiesis.pedido.domain.port.CustomizacaoServicePort customizacao) {
        this.pedidoRepository = pedidoRepository;
        this.catalogo = catalogo;
        this.notificacao = notificacao;
        this.customizacao = customizacao;
    }

    public Pedido executar(String clienteEmail, List<ItemPedido> itens) {
        if (clienteEmail == null || clienteEmail.isBlank()) {
            throw new IllegalArgumentException("E-mail do cliente é obrigatório.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter ao menos um item.");
        }

        for (ItemPedido item : itens) {
            if (item == null || item.getProdutoId() == null || item.getQuantidade() == null
                    || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("Cada item deve ter produto e quantidade maior que zero.");
            }
            // Nome e preço vêm do catálogo, evitando usar valores enviados pelo cliente.
            CatalogoServicePort.Produto produto = catalogo.buscarProduto(item.getProdutoId());
            if (produto == null || produto.preco() == null || produto.preco().signum() < 0) {
                throw new IllegalArgumentException("Produto inválido no catálogo: " + item.getProdutoId());
            }
            item.setNomeProduto(produto.nome());
            var ids = item.getCustomizacaoIds();
            if (ids.size() > 20 || ids.stream().anyMatch(id -> id == null || id <= 0)
                    || new java.util.HashSet<>(ids).size() != ids.size()) {
                throw new IllegalArgumentException("Identificadores de customização inválidos ou duplicados.");
            }
            var escolhidas = new java.util.ArrayList<com.poiesis.pedido.domain.entity.CustomizacaoEscolhida>();
            var tipos = new java.util.HashSet<String>();
            if (!ids.isEmpty()) {
                var ativas = customizacao.listarAtivas(item.getProdutoId());
                for (Long id : ids) {
                    var opcao = ativas.stream().filter(o -> id.equals(o.id())).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Customização indisponível para este produto: " + id));
                    if (opcao.precoAdicional() == null || opcao.precoAdicional().signum() < 0
                            || opcao.tipo() == null || !tipos.add(opcao.tipo().trim().toUpperCase(java.util.Locale.ROOT))) {
                        throw new IllegalArgumentException("Selecione uma opção válida por tipo de customização.");
                    }
                    escolhidas.add(opcao);
                }
            }
            item.setCustomizacoes(escolhidas);
            var adicionais = escolhidas.stream().map(com.poiesis.pedido.domain.entity.CustomizacaoEscolhida::precoAdicional)
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
            item.setPrecoUnitario(produto.preco().add(adicionais));
        }

        Pedido pedido = new Pedido(null, clienteEmail, itens);
        Pedido salvo = pedidoRepository.salvar(pedido);
        // Publica após salvar para incluir o ID; banco e mensageria são operações separadas.
        notificacao.notificarPedidoCriado(salvo);
        return salvo;
    }
}
