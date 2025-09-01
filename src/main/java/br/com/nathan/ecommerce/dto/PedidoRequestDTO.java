package br.com.nathan.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record PedidoRequestDTO(
        @NotNull(message = "ID do cliente é obrigatório")
        UUID clienteId,
        
        @NotEmpty(message = "Lista de itens não pode estar vazia")
        @Valid
        List<ItemPedidoRequestDTO> itens
) {}
