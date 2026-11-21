package com.dt2.streetwear.domain.dto;

import com.dt2.streetwear.domain.entity.FormaPagamento;
import com.dt2.streetwear.domain.entity.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(
    Long id,
    Long clienteId,
    String clienteNome,
    LocalDateTime dataVenda,
    FormaPagamento formaPagamento,
    StatusVenda status,
    BigDecimal valorTotal,
    List<ItemVendaResponse> itens
) {}
