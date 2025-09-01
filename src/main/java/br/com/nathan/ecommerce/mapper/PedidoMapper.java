package br.com.nathan.ecommerce.mapper;

import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.dto.PedidoResponseDTO;

import java.util.stream.Collectors;

public class PedidoMapper {
    
    public static PedidoResponseDTO toResponseDTO(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getCliente().getId(),
                pedido.getCliente().getNome(),
                pedido.getItens().stream()
                        .map(ItemPedidoMapper::toResponseDTO)
                        .collect(Collectors.toList()),
                pedido.getValorTotal(),
                pedido.getStatus(),
                pedido.getDataCadastro(),
                pedido.getDataAtualizacao()
        );
    }
}
