package br.com.nathan.ecommerce.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TicketMedioDTO(
        UUID clienteId,
        String nomeCliente,
        BigDecimal ticketMedio
) {}
