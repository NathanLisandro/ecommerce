package br.com.nathan.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopClienteDTO(
        UUID clienteId,
        String nomeCliente,
        Long totalPedidos,
        BigDecimal valorTotalComprado
) {}
