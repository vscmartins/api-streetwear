package com.dt2.streetwear.domain.dto;

public record MarcaResponse(
    Long id,
    String nome,
    String paisOrigem,
    String descricao
) {}
