package com.dt2.streetwear.domain.dto;

public record VariacaoResponse(
    Long id,
    String sku,
    String tamanho,
    String cor,
    Integer estoque,
    Integer estoqueMinimo,
    Long produtoId,
    String produtoNome
) {}
