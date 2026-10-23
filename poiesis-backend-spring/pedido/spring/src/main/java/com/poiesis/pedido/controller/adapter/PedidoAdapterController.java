package com.poiesis.pedido.controller.adapter;

import com.poiesis.pedido.entity.ItemPedido;
import com.poiesis.pedido.entity.Pedido;
import com.poiesis.pedido.controller.dto.request.ItemPedidoRequest;
import com.poiesis.pedido.controller.dto.response.ItemPedidoResponse;
import com.poiesis.pedido.controller.dto.response.PedidoResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PedidoAdapterController {

    public ItemPedido toDomainItem(ItemPedidoRequest dto) {
        if (dto == null) return null;
        var item = new ItemPedido(dto.getProdutoId(), null, dto.getQuantidade(), null);
        item.setCustomizacaoIds(dto.getCustomizacaoIds());
        item.setTextoPersonalizado(dto.getTextoPersonalizado());
        item.setReferenciaImagem(dto.getReferenciaImagem());
        return item;
    }

    public List<ItemPedido> toDomainItemList(List<ItemPedidoRequest> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(this::toDomainItem).collect(Collectors.toList());
    }

    public ItemPedidoResponse toResponseItem(ItemPedido item) {
        if (item == null) return null;
        var response = new ItemPedidoResponse(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
        response.setCustomizacoes(item.getCustomizacoes());
        response.setTextoPersonalizado(item.getTextoPersonalizado());
        response.setReferenciaImagem(item.getReferenciaImagem());
        return response;
    }

    public PedidoResponse toResponse(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoResponse> itensDTO = pedido.getItens() == null ? List.of() :
                pedido.getItens().stream().map(this::toResponseItem).collect(Collectors.toList());

        return new PedidoResponse(
                pedido.getId(),
                pedido.getClienteEmail(),
                itensDTO,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao()
        );
    }
}
