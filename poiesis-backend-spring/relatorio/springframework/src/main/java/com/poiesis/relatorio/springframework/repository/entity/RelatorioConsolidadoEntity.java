package com.poiesis.relatorio.springframework.repository.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_relatorio_consolidado", uniqueConstraints = @UniqueConstraint(name = "uk_relatorio_pedido", columnNames = "pedido_id"))
public class RelatorioConsolidadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    private LocalDate dataConsolidacao;
    private Long totalPedidos;
    private BigDecimal faturamentoTotal;
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    public RelatorioConsolidadoEntity() {}

    public RelatorioConsolidadoEntity(LocalDate dataConsolidacao, Long totalPedidos, BigDecimal faturamentoTotal, Long pedidoId) {
        this.dataConsolidacao = dataConsolidacao;
        this.totalPedidos = totalPedidos;
        this.faturamentoTotal = faturamentoTotal;
        this.pedidoId = pedidoId;
    }

    public Long getId() { return id; }
    public LocalDate getDataConsolidacao() { return dataConsolidacao; }
    public Long getTotalPedidos() { return totalPedidos; }
    public BigDecimal getFaturamentoTotal() { return faturamentoTotal; }
    public Long getPedidoId() { return pedidoId; }
}
