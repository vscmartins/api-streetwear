package com.dt2.streetwear.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarImagemRequest(
    @NotBlank(message = "A URL da imagem e obrigatoria")
    @Size(max = 500, message = "A URL da imagem deve ter no maximo 500 caracteres")
    String imagemUrl
) {}
