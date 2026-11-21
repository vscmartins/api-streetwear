package com.dt2.streetwear.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AtualizarItemRequest(
    @NotNull(message = "A quantidade e obrigatoria")
    @Min(value = 1, message = "A quantidade minima e 1")
    Integer quantidade
) {}
