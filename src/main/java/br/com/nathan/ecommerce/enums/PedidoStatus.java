package br.com.nathan.ecommerce.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum PedidoStatus {

    PENDENTE(1L, "PENDENTE"),
    APROVADO(2L, "APROVADO"),
    REPROVADO(3L, "REPROVADO"),
    CANCELADO(4L, "CANCELADO");

    private Long id;
    private String status;

}
