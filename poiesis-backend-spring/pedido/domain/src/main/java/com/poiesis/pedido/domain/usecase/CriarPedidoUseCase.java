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

    public CriarPedidoUseCase(PedidoRepositoryPort pedidoRepository, CatalogoServicePort catalogo,
                              NotificacaoEventPort notificacao) {
        this.pedidoRepository = pedidoRepository;
        this.catalogo = catalogo;
        this.notificacao = notificacao;
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
            CatalogoServicePort.Produto produto = catalogo.buscarProduto(item.getProdutoId());
            if (produto == null || produto.preco() == null || produto.preco().signum() < 0) {
                throw new IllegalArgumentException("Produto inválido no catálogo: " + item.getProdutoId());
            }
            item.setNomeProduto(produto.nome());
            item.setPrecoUnitario(produto.preco());
        }

        Pedido pedido = new Pedido(null, clienteEmail, itens);
        Pedido salvo = pedidoRepository.salvar(pedido);
        notificacao.notificarPedidoCriado(salvo);
        return salvo;
    }
}
