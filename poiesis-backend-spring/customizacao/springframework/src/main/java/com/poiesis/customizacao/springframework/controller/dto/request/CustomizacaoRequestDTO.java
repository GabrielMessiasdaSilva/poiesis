package com.poiesis.customizacao.springframework.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CustomizacaoRequestDTO(
        @NotNull @Positive Long produtoId,
        @NotBlank @Size(max = 80) String tipo,
        @NotBlank @Size(max = 160) String nome,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precoAdicional
) {
}
