package com.dt2.streetwear.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MarcaRequest(
    @NotBlank(message = "O nome da marca e obrigatorio")
    @Size(max = 60, message = "O nome da marca deve ter no maximo 60 caracteres")
    String nome,

    @Size(max = 60, message = "O pais de origem deve ter no maximo 60 caracteres")
    String paisOrigem,

    @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
    String descricao
) {}
