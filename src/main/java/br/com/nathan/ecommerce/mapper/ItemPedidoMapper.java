package br.com.nathan.ecommerce.mapper;

import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.dto.ItemPedidoResponseDTO;

public class ItemPedidoMapper {
    
    public static ItemPedidoResponseDTO toResponseDTO(ItemPedido itemPedido) {
        return new ItemPedidoResponseDTO(
                itemPedido.getId(),
                itemPedido.getProdutos().getId(),
                itemPedido.getProdutos().getNome(),
                itemPedido.getQuantidade(),
                itemPedido.getPrecoUnitario(),
                itemPedido.getPrecoTotal()
        );
    }
}
