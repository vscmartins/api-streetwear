package com.dt2.streetwear.domain.dto;

import com.dt2.streetwear.domain.entity.StatusVenda;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(
    @NotNull(message = "O novo status e obrigatorio")
    StatusVenda status
) {}
