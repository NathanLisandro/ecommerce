package br.com.nathan.ecommerce.dto;

import br.com.nathan.ecommerce.enums.PedidoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        UUID clienteId,
        String nomeCliente,
        List<ItemPedidoResponseDTO> itens,
        BigDecimal valorTotal,
        PedidoStatus status,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {}
