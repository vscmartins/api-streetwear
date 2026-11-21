package com.dt2.streetwear.domain.dto;

import com.dt2.streetwear.domain.entity.Categoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
    @NotBlank(message = "O nome do produto e obrigatorio")
    @Size(max = 120, message = "O nome do produto deve ter no maximo 120 caracteres")
    String nome,

    @NotBlank(message = "A descricao e obrigatoria")
    @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
    String descricao,

    @NotNull(message = "O preco e obrigatorio")
    @DecimalMin(value = "0.01", message = "O preco deve ser maior que zero")
    BigDecimal preco,

    @NotNull(message = "A categoria e obrigatoria")
    Categoria categoria,

    Boolean ativo,

    @Size(max = 500, message = "A URL da imagem deve ter no maximo 500 caracteres")
    String imagemUrl,

    @NotNull(message = "O ID da marca e obrigatorio")
    Long marcaId
) {}
