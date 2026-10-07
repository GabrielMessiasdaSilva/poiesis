package com.poiesis.producao.springframework.controller.dto.request;

import com.poiesis.producao.domain.entity.StatusProducao;

public record AtualizarStatusRequestDTO(StatusProducao status) {
}
