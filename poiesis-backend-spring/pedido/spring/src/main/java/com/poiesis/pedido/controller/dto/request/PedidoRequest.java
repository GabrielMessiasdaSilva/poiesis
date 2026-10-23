package com.poiesis.pedido.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class PedidoRequest {
    @Valid
    @NotEmpty(message = "O pedido deve conter ao menos um item.")
    private List<ItemPedidoRequest> itens;

    public PedidoRequest() {}

    public List<ItemPedidoRequest> getItens() { return itens; }
    public void setItens(List<ItemPedidoRequest> itens) { this.itens = itens; }
}
