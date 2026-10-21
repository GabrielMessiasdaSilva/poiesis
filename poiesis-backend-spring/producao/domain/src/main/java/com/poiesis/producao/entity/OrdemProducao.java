package com.poiesis.producao.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrdemProducao {
    private Long id;
    private Long pedidoId;
    private String clienteEmail;
    private StatusProducao status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataAtualizacao;
    private List<HistoricoStatusProducao> historico = new ArrayList<>();

    public OrdemProducao() {}

    public OrdemProducao(Long id, Long pedidoId, String clienteEmail, StatusProducao status, LocalDateTime dataInicio, LocalDateTime dataAtualizacao) {
        this(id, pedidoId, clienteEmail, status, dataInicio, dataAtualizacao, List.of());
    }

    public OrdemProducao(Long id, Long pedidoId, String clienteEmail, StatusProducao status, LocalDateTime dataInicio,
            LocalDateTime dataAtualizacao, List<HistoricoStatusProducao> historico) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataAtualizacao = dataAtualizacao;
        this.historico = historico == null || historico.isEmpty()
                ? new ArrayList<>(List.of(new HistoricoStatusProducao(status, dataAtualizacao)))
                : new ArrayList<>(historico);
    }

    public OrdemProducao(Long pedidoId, String clienteEmail) {
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.status = StatusProducao.PENDENTE;
        this.dataInicio = LocalDateTime.now();
        this.dataAtualizacao = this.dataInicio;
        this.historico = new ArrayList<>(List.of(new HistoricoStatusProducao(status, dataInicio)));
    }

    public void atualizarStatus(StatusProducao novoStatus) {
        this.status = novoStatus;
        this.dataAtualizacao = LocalDateTime.now();
        this.historico.add(new HistoricoStatusProducao(novoStatus, dataAtualizacao));
    }

    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public String getClienteEmail() { return clienteEmail; }
    public StatusProducao getStatus() { return status; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public List<HistoricoStatusProducao> getHistorico() { return List.copyOf(historico); }
}