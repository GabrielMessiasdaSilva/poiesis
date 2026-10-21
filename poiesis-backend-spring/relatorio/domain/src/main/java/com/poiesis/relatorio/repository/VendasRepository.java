package com.poiesis.relatorio.repository;

import com.poiesis.relatorio.entity.MetricaVendas;
import com.poiesis.relatorio.entity.RegistroVenda;
import java.time.LocalDate;

public interface VendasRepository {
    MetricaVendas obterMetricasVendas(LocalDate inicio, LocalDate fim);
    boolean existePorPedidoId(Long pedidoId);
    void registrar(RegistroVenda venda);
}
