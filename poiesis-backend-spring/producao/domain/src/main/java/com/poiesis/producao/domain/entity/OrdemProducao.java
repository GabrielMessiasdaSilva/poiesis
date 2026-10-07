package com.poiesis.producao.domain.entity;

import java.time.LocalDateTime;

public class OrdemProducao {
    private Long id;
    private Long pedidoId;
    private String clienteEmail;
    private StatusProducao status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataAtualizacao;

    public OrdemProducao() {}

    public OrdemProducao(Long id, Long pedidoId, String clienteEmail, StatusProducao status, LocalDateTime dataInicio, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataAtualizacao = dataAtualizacao;
    }

    public OrdemProducao(Long pedidoId, String clienteEmail) {
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.status = StatusProducao.PENDENTE;
        this.dataInicio = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }

    public void atualizarStatus(StatusProducao novoStatus) {
        this.status = novoStatus;
        this.dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getClienteEmail() { return clienteEmail; }
    public StatusProducao getStatus() { return status; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}