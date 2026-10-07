package com.poiesis.pedido.springframework.controller.dto.request;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class PedidoRequestDTO {
    @Valid
    @NotEmpty(message = "O pedido deve conter ao menos um item.")
    private List<ItemPedidoRequestDTO> itens;

    public PedidoRequestDTO() {}

    public List<ItemPedidoRequestDTO> getItens() { return itens; }
    public void setItens(List<ItemPedidoRequestDTO> itens) { this.itens = itens; }
}
