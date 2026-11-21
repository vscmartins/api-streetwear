package com.dt2.streetwear.domain.dto;

import com.dt2.streetwear.domain.entity.Categoria;

import java.math.BigDecimal;

public record ProdutoResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal preco,
    Categoria categoria,
    Boolean ativo,
    String imagemUrl,
    Long marcaId,
    String marcaNome,
    Integer estoqueTotal
) {}
