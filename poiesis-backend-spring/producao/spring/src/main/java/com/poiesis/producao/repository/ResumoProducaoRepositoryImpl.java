package com.poiesis.producao.repository;

import com.poiesis.producao.entity.ResumoProducao;
import com.poiesis.producao.entity.StatusProducao;
import com.poiesis.producao.repository.ResumoProducaoRepository;
import com.poiesis.producao.repository.jpa.OrdemProducaoRepositoryWithJpa;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ResumoProducaoRepositoryImpl implements ResumoProducaoRepository {
    private final OrdemProducaoRepositoryWithJpa repository;

    public ResumoProducaoRepositoryImpl(OrdemProducaoRepositoryWithJpa repository) { this.repository = repository; }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ResumoProducao consultar() {
        return new ResumoProducao(repository.countByStatus(StatusProducao.PENDENTE),
                repository.countByStatus(StatusProducao.EM_CORTE), repository.countByStatus(StatusProducao.EM_COSTURA),
                repository.countByStatus(StatusProducao.ACABAMENTO), repository.countByStatus(StatusProducao.CONCLUIDO));
    }
}
