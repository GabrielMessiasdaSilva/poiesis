package com.poiesis.producao.service;

import com.poiesis.producao.entity.ResumoProducao;
import com.poiesis.producao.repository.ResumoProducaoRepository;

public class ResumoProducaoService {
    private final ResumoProducaoRepository resumo;

    public ResumoProducaoService(ResumoProducaoRepository resumo) { this.resumo = resumo; }

    public ResumoProducao consultar() { return resumo.consultar(); }
}
