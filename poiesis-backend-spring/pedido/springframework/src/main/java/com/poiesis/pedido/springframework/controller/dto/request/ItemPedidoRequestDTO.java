package com.poiesis.pedido.springframework.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemPedidoRequestDTO {
    @NotNull(message = "O produto é obrigatório.")
    @Positive(message = "O identificador do produto deve ser positivo.")
    private Long produtoId;
    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantidade;
    @jakarta.validation.constraints.Size(max = 20, message = "Selecione até 20 customizações por item.")
    private java.util.List<@NotNull @Positive Long> customizacaoIds = java.util.List.of();

    public java.util.List<Long> getCustomizacaoIds() { return customizacaoIds; }
    public void setCustomizacaoIds(java.util.List<Long> ids) { customizacaoIds = ids == null ? java.util.List.of() : ids; }

    public ItemPedidoRequestDTO() {}

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
