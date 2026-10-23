package com.poiesis.pedido.service;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import com.poiesis.pedido.entity.ItemPedido;
import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.integration.CatalogoIntegration;
import com.poiesis.pedido.messaging.PedidoPublisher;
import com.poiesis.pedido.repository.PedidoRepository;
import com.poiesis.pedido.service.SelecionarCustomizacoesService;
import java.math.BigDecimal;
import java.util.List;

public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final CatalogoIntegration catalogo;
    private final PedidoPublisher notificacao;
    private final SelecionarCustomizacoesService selecionarCustomizacoes;

    public PedidoService(PedidoRepository pedidoRepository, CatalogoIntegration catalogo,
                              PedidoPublisher notificacao,
                              SelecionarCustomizacoesService selecionarCustomizacoes) {
        this.pedidoRepository = pedidoRepository;
        this.catalogo = catalogo;
        this.notificacao = notificacao;
        this.selecionarCustomizacoes = selecionarCustomizacoes;
    }

    public Pedido criarPedido(String clienteEmail, List<ItemPedido> itens) {
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
            CatalogoIntegration.Produto produto = catalogo.buscarProduto(item.getProdutoId());
            if (produto == null || produto.preco() == null || produto.preco().signum() < 0) {
                throw new IllegalArgumentException("Produto inválido no catálogo: " + item.getProdutoId());
            }
            item.setNomeProduto(produto.nome());
            var escolhidas = selecionarCustomizacoes.selecionar(item.getProdutoId(), item.getCustomizacaoIds());
            item.setCustomizacoes(escolhidas);
            var adicionais = escolhidas.stream().map(CustomizacaoEscolhida::precoAdicional)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            item.setPrecoUnitario(produto.preco().add(adicionais));
        }

        Pedido pedido = new Pedido(null, clienteEmail, itens);
        Pedido salvo = pedidoRepository.salvar(pedido);
        // Publica após salvar para incluir o ID; banco e mensageria são operações separadas.
        notificacao.notificarPedidoCriado(salvo);
        return salvo;
    }
}
