package com.poiesis.pedido.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private Long id;
    private String clienteEmail;
    private List<ItemPedido> itens = new ArrayList<>();
    private BigDecimal valorTotal;
    private StatusPedido status;
    private LocalDateTime dataCriacao;

    public Pedido() {}

    public Pedido(Long id, String clienteEmail, List<ItemPedido> itens) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.itens = itens != null ? itens : new ArrayList<>();
        this.status = StatusPedido.CRIADO;
        this.dataCriacao = LocalDateTime.now();
        this.calcularValorTotal();
    }

    public Pedido(Long id, String clienteEmail, List<ItemPedido> itens, BigDecimal valorTotal,
                  StatusPedido status, LocalDateTime dataCriacao) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.itens = itens != null ? itens : new ArrayList<>();
        this.valorTotal = valorTotal;
        this.status = status;
        this.dataCriacao = dataCriacao;
    }

    public void calcularValorTotal() {
        this.valorTotal = itens.stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }
}
