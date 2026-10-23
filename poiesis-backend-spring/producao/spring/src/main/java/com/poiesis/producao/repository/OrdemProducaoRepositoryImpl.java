package com.poiesis.producao.repository;

import com.poiesis.producao.entity.OrdemProducao;
import com.poiesis.producao.repository.OrdemProducaoRepository;
import com.poiesis.producao.repository.adapter.OrdemProducaoRepositoryAdapter;
import com.poiesis.producao.repository.jpa.OrdemProducaoRepositoryWithJpa;
import com.poiesis.producao.repository.orm.OrdemProducaoOrmJpa;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;

@Component
public class OrdemProducaoRepositoryImpl implements OrdemProducaoRepository {

    private final OrdemProducaoRepositoryWithJpa repository;
    private final OrdemProducaoRepositoryAdapter mapper;

    public OrdemProducaoRepositoryImpl(OrdemProducaoRepositoryWithJpa repository, OrdemProducaoRepositoryAdapter mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrdemProducao salvar(OrdemProducao ordemProducao) {
        OrdemProducaoOrmJpa entity;
        if (ordemProducao.getId() == null) {
            entity = mapper.toOrm(ordemProducao);
        } else {
            // Carrega a entidade gerenciada para preservar a versão usada pelo JPA na atualização.
            entity = repository.findById(ordemProducao.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ordem de produção não encontrada ID: " + ordemProducao.getId()));
            if (ordemProducao.getVersion() == null || !ordemProducao.getVersion().equals(entity.getVersion())) {
                throw new OptimisticLockingFailureException("Ordem de produção alterada por outra operação.");
            }
            entity.setPedidoId(ordemProducao.getPedidoId());
            entity.setClienteEmail(ordemProducao.getClienteEmail());
            entity.setStatus(ordemProducao.getStatus());
            entity.setDataInicio(ordemProducao.getDataInicio());
            entity.setDataAtualizacao(ordemProducao.getDataAtualizacao());
                entity.setHistorico(ordemProducao.getHistorico().stream()
                    .map(item -> new com.poiesis.producao.repository.orm.HistoricoStatusOrmJpa(item.status(), item.dataHora())).toList());
        }
        OrdemProducaoOrmJpa saved = repository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OrdemProducao> buscarPorId(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<OrdemProducao> buscarPorPedidoId(Long pedidoId) {
        return repository.findByPedidoId(pedidoId).map(mapper::toDomain);
    }

    @Override
    public List<OrdemProducao> listarTodas() {
        return repository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
