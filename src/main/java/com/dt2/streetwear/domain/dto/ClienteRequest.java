package com.dt2.streetwear.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres")
    String nome,

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(regexp = "^\\d{11}$", message = "O CPF deve conter exatamente 11 digitos numericos")
    String cpf,

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "O e-mail informado e invalido")
    @Size(max = 120, message = "O e-mail deve ter no maximo 120 caracteres")
    String email,

    @Size(max = 20, message = "O telefone deve ter no maximo 20 caracteres")
    String telefone,

    @Size(max = 40, message = "O Instagram deve ter no maximo 40 caracteres")
    String instagram
) {}
