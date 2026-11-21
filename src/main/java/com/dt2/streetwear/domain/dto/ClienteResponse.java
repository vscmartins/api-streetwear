package com.dt2.streetwear.domain.dto;

public record ClienteResponse(
    Long id,
    String nome,
    String cpf,
    String email,
    String telefone,
    String instagram
) {}
