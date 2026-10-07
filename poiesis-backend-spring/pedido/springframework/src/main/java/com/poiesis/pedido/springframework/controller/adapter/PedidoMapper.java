package com.poiesis.pedido.springframework.controller.adapter;

import com.poiesis.pedido.domain.entity.ItemPedido;
import com.poiesis.pedido.domain.entity.Pedido;
import com.poiesis.pedido.springframework.controller.dto.request.ItemPedidoRequestDTO;
import com.poiesis.pedido.springframework.controller.dto.request.PedidoRequestDTO;
import com.poiesis.pedido.springframework.controller.dto.response.ItemPedidoResponseDTO;
import com.poiesis.pedido.springframework.controller.dto.response.PedidoResponseDTO;
import com.poiesis.pedido.springframework.repository.entity.ItemPedidoEntity;
import com.poiesis.pedido.springframework.repository.entity.PedidoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PedidoMapper {

    // --- Mapeamentos do DTO de Request para Domínio ---
    public ItemPedido toDomainItem(ItemPedidoRequestDTO dto) {
        if (dto == null) return null;
        return new ItemPedido(dto.getProdutoId(), null, dto.getQuantidade(), null);
    }

    public List<ItemPedido> toDomainItemList(List<ItemPedidoRequestDTO> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream().map(this::toDomainItem).collect(Collectors.toList());
    }

    // --- Mapeamentos do Domínio para DTO de Response ---
    public ItemPedidoResponseDTO toResponseItemDTO(ItemPedido item) {
        if (item == null) return null;
        return new ItemPedidoResponseDTO(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal()
        );
    }

    public PedidoResponseDTO toResponseDTO(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens() == null ? List.of() :
                pedido.getItens().stream().map(this::toResponseItemDTO).collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getClienteEmail(),
                itensDTO,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao()
        );
    }

    // --- Mapeamentos do Domínio para Entidade JPA ---
    public ItemPedidoEntity toEntityItem(ItemPedido item) {
        if (item == null) return null;
        return new ItemPedidoEntity(
                item.getProdutoId(),
                item.getNomeProduto(),
                item.getQuantidade(),
                item.getPrecoUnitario()
        );
    }

    public PedidoEntity toEntity(Pedido pedido) {
        if (pedido == null) return null;
        List<ItemPedidoEntity> itensEntity = pedido.getItens() == null ? List.of() :
                pedido.getItens().stream().map(this::toEntityItem).collect(Collectors.toList());

        return new PedidoEntity(
                pedido.getId(),
                pedido.getClienteEmail(),
                itensEntity,
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCriacao()
        );
    }

    // --- Mapeamentos da Entidade JPA para Domínio ---
    public ItemPedido toDomainItem(ItemPedidoEntity entity) {
        if (entity == null) return null;
        return new ItemPedido(
                entity.getProdutoId(),
                entity.getNomeProduto(),
                entity.getQuantidade(),
                entity.getPrecoUnitario()
        );
    }

    public Pedido toDomain(PedidoEntity entity) {
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
