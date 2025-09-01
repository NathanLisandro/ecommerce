package br.com.nathan.ecommerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        String categoria,
        Long quantidadeEstoque,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {}
