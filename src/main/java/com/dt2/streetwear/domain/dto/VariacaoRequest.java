package com.dt2.streetwear.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VariacaoRequest(
    @NotBlank(message = "O SKU e obrigatorio")
    @Size(max = 40, message = "O SKU deve ter no maximo 40 caracteres")
    String sku,

    @NotBlank(message = "O tamanho e obrigatorio")
    @Size(max = 10, message = "O tamanho deve ter no maximo 10 caracteres")
    String tamanho,

    @NotBlank(message = "A cor e obrigatoria")
    @Size(max = 40, message = "A cor deve ter no maximo 40 caracteres")
    String cor,

    @NotNull(message = "O estoque e obrigatorio")
    @Min(value = 0, message = "O estoque nao pode ser negativo")
    Integer estoque,

    @NotNull(message = "O estoque minimo e obrigatorio")
    @Min(value = 0, message = "O estoque minimo nao pode ser negativo")
    Integer estoqueMinimo,

    @NotNull(message = "O ID do produto e obrigatorio")
    Long produtoId
) {}
