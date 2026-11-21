package com.dt2.streetwear.domain.dto;

import com.dt2.streetwear.domain.entity.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record VendaRequest(
    @NotNull(message = "O cliente e obrigatorio")
    Long clienteId,

    @NotNull(message = "A forma de pagamento e obrigatoria")
    FormaPagamento formaPagamento,

    @NotEmpty(message = "A venda deve conter pelo menos um item")
    @Valid
    List<ItemVendaRequest> itens
) {}
