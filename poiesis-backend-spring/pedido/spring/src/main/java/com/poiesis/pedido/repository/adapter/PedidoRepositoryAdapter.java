package com.poiesis.pedido.repository.adapter;

import com.poiesis.pedido.entity.CustomizacaoEscolhida;
import com.poiesis.pedido.entity.ItemPedido;
import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.repository.orm.CustomizacaoEscolhidaOrmJpa;
import com.poiesis.pedido.repository.orm.ItemPedidoOrmJpa;
import com.poiesis.pedido.repository.orm.PedidoOrmJpa;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PedidoRepositoryAdapter {

    public ItemPedidoOrmJpa toOrmItem(ItemPedido item) {
        if (item == null) return null;
        var entity = new ItemPedidoOrmJpa(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
        entity.setCustomizacoes(item.getCustomizacoes().stream()
                .map(opcao -> new CustomizacaoEscolhidaOrmJpa(
                        opcao.id(), opcao.tipo(), opcao.nome(), opcao.precoAdicional())).toList());
        entity.setTextoPersonalizado(item.getTextoPersonalizado());
        entity.setReferenciaImagem(item.getReferenciaImagem());
        return entity;
    }

    public PedidoOrmJpa toOrm(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoOrmJpa> itensEntity = pedido.getItens() == null ? List.of() :
                pedido.getItens().stream().map(this::toOrmItem).collect(Collectors.toList());

        return new PedidoOrmJpa(
                pedido.getId(),
                pedido.getClienteEmail(),
                itensEntity,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao()
        );
    }

    public ItemPedido toDomainItem(ItemPedidoOrmJpa entity) {
        if (entity == null) return null;
        var item = new ItemPedido(
                entity.getProdutoId(),
                entity.getNomeProduto(),
                entity.getQuantidade(),
                entity.getPrecoUnitario()
        );
        item.setCustomizacoes(entity.getCustomizacoes().stream()
                .map(opcao -> new CustomizacaoEscolhida(
                        opcao.getCustomizacaoId(), opcao.getTipo(), opcao.getNome(), opcao.getPrecoAdicional())).toList());
        item.setTextoPersonalizado(entity.getTextoPersonalizado());
        item.setReferenciaImagem(entity.getReferenciaImagem());
        return item;
    }

    public Pedido toDomain(PedidoOrmJpa entity) {
        if (entity == null) return null;
        List<ItemPedido> itensDomain = entity.getItens() == null ? List.of() :
                entity.getItens().stream().map(this::toDomainItem).collect(Collectors.toList());

        return new Pedido(
                entity.getId(),
                entity.getClienteEmail(),
                itensDomain,
                entity.getValorTotal(),
                entity.getStatus(),
                entity.getDataCriacao()
        );
    }
}
