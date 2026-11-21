package com.dt2.streetwear.domain.dto;

import java.math.BigDecimal;

public record ItemVendaResponse(
    Long id,
    Long variacaoId,
    String variacaoSku,
    String tamanho,
    String cor,
    String produtoNome,
    Integer quantidade,
    BigDecimal precoUnitario,
    BigDecimal subtotal
) {}
