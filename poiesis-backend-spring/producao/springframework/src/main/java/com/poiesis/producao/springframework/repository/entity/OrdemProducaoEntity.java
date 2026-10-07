package com.poiesis.producao.springframework.repository.entity;

import com.poiesis.producao.domain.entity.StatusProducao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
// A unicidade por pedido impede registros duplicados, inclusive em inserções concorrentes.
@Table(name = "tb_ordem_producao", uniqueConstraints = @UniqueConstraint(name = "uk_ordem_pedido", columnNames = "pedido_id"))
public class OrdemProducaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Controle otimista: o JPA detecta versões divergentes ao atualizar a mesma entidade.
    @Version
    private Long version;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;
    private String clienteEmail;

    @Enumerated(EnumType.STRING)
    private StatusProducao status;

    private LocalDateTime dataInicio;
    private LocalDateTime dataAtualizacao;

    public OrdemProducaoEntity() {}

    public OrdemProducaoEntity(Long id, Long pedidoId, String clienteEmail, StatusProducao status, LocalDateTime dataInicio, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.status = status;
        this.dataInicio = dataInicio;
        this.dataAtualizacao = dataAtualizacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public StatusProducao getStatus() { return status; }
    public void setStatus(StatusProducao status) { this.status = status; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
