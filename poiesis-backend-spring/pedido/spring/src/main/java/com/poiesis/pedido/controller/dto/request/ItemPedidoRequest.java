package com.poiesis.pedido.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.List;

public class ItemPedidoRequest {
    @NotNull(message = "O produto é obrigatório.")
    @Positive(message = "O identificador do produto deve ser positivo.")
    private Long produtoId;
    @NotNull(message = "A quantidade é obrigatória.")
    @Positive(message = "A quantidade deve ser maior que zero.")
    private Integer quantidade;
    @Size(max = 20, message = "Selecione até 20 customizações por item.")
    private List<@NotNull @Positive Long> customizacaoIds = List.of();
    @Size(max = 300, message = "O texto personalizado deve ter até 300 caracteres.")
    private String textoPersonalizado;
    @Size(max = 2048, message = "A referência da arte deve ter até 2048 caracteres.")
    @Pattern(regexp = "(https?://\\S+|asset://poiesis/[a-z0-9-]+)", message = "A arte deve ser uma URL HTTP/HTTPS ou uma arte da biblioteca Poiesis.")
    private String referenciaImagem;

    public List<Long> getCustomizacaoIds() { return customizacaoIds; }
    public void setCustomizacaoIds(List<Long> ids) { customizacaoIds = ids == null ? List.of() : ids; }
    public String getTextoPersonalizado() { return textoPersonalizado; }
    public void setTextoPersonalizado(String textoPersonalizado) { this.textoPersonalizado = textoPersonalizado; }
    public String getReferenciaImagem() { return referenciaImagem; }
    public void setReferenciaImagem(String referenciaImagem) { this.referenciaImagem = referenciaImagem; }

    public ItemPedidoRequest() {}

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
