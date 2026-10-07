package com.poiesis.producao.springframework.repository;

import com.poiesis.producao.domain.entity.OrdemProducao;
import com.poiesis.producao.domain.repository.OrdemProducaoRepository;
import com.poiesis.producao.springframework.controller.adapter.ProducaoMapper;
import com.poiesis.producao.springframework.repository.entity.OrdemProducaoEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OrdemProducaoPersistenceAdapter implements OrdemProducaoRepository {

    private final SpringDataOrdemProducaoRepository repository;
    private final ProducaoMapper mapper;

    public OrdemProducaoPersistenceAdapter(SpringDataOrdemProducaoRepository repository, ProducaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OrdemProducao salvar(OrdemProducao ordemProducao) {
        OrdemProducaoEntity entity;
        if (ordemProducao.getId() == null) {
            entity = mapper.toEntity(ordemProducao);
        } else {
            entity = repository.findById(ordemProducao.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Ordem de produção não encontrada ID: " + ordemProducao.getId()));
            entity.setPedidoId(ordemProducao.getPedidoId());
            entity.setClienteEmail(ordemProducao.getClienteEmail());
            entity.setStatus(ordemProducao.getStatus());
            entity.setDataInicio(ordemProducao.getDataInicio());
            entity.setDataAtualizacao(ordemProducao.getDataAtualizacao());
        }
        OrdemProducaoEntity saved = repository.save(entity);
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
