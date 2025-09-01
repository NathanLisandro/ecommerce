package br.com.nathan.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoResponseDTO(
        UUID id,
        UUID produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal precoTotal
) {}
