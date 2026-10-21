package com.poiesis.relatorio.service;

import com.poiesis.relatorio.entity.RegistroVenda;
import com.poiesis.relatorio.repository.VendasRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistroVendaService {
    private final VendasRepository vendas;

    public RegistroVendaService(VendasRepository vendas) { this.vendas = vendas; }

    public void registrar(Long pedidoId, BigDecimal valorTotal, LocalDateTime dataCriacao) {
        if (pedidoId == null || pedidoId <= 0) throw new IllegalArgumentException("Evento de pedido sem identificador válido.");
        if (vendas.existePorPedidoId(pedidoId)) return;
        if (valorTotal == null || valorTotal.signum() < 0) throw new IllegalArgumentException("Evento sem valor válido.");
        if (dataCriacao == null) throw new IllegalArgumentException("Evento sem data de criação do pedido.");
        vendas.registrar(new RegistroVenda(pedidoId, dataCriacao.toLocalDate(), valorTotal));
    }
}
