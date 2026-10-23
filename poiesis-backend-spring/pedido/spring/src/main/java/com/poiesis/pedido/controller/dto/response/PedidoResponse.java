package com.poiesis.pedido.controller.dto.response;

import com.poiesis.pedido.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoResponse {
    private Long id;
    private String clienteEmail;
    private List<ItemPedidoResponse> itens;
    private BigDecimal valorTotal;
    private StatusPedido status;
    private LocalDateTime dataCriacao;

    public PedidoResponse() {}

    public PedidoResponse(Long id, String clienteEmail, List<ItemPedidoResponse> itens, BigDecimal valorTotal, StatusPedido status, LocalDateTime dataCriacao) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.itens = itens;
        this.valorTotal = valorTotal;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public List<ItemPedidoResponse> getItens() { return itens; }
    public void setItens(List<ItemPedidoResponse> itens) { this.itens = itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}