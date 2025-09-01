package br.com.nathan.ecommerce.fixtures;

import br.com.nathan.ecommerce.domain.Cliente;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PedidoFixture {

    public static Pedido createValidPedido(Cliente cliente) {
        Pedido pedido = createPedidoWithParams(cliente, PedidoStatus.PENDENTE, new BigDecimal("1799.98"));
        
        // Adicionar itens válidos ao pedido
        List<ItemPedido> itens = new ArrayList<>();
        
        // Criar produtos fictícios para os itens
        Produtos produto1 = ProdutoFixture.createValidProduto();
        produto1.setId(UUID.randomUUID());
        produto1.setPreco(new BigDecimal("899.99"));
        
        Produtos produto2 = ProdutoFixture.createValidProduto();
        produto2.setId(UUID.randomUUID());
        produto2.setPreco(new BigDecimal("899.99"));
        
        // Criar itens do pedido
        ItemPedido item1 = createItemPedidoWithParams(pedido, produto1, 1, produto1.getPreco());
        ItemPedido item2 = createItemPedidoWithParams(pedido, produto2, 1, produto2.getPreco());
        
        itens.add(item1);
        itens.add(item2);
        
        pedido.setItens(itens);
        return pedido;
    }

    public static Pedido createPedidoWithId(UUID id, Cliente cliente) {
        Pedido pedido = Pedido.builder()
                .id(id)
                .cliente(cliente)
                .status(PedidoStatus.PENDENTE)
                .valorTotal(new BigDecimal("1799.98"))
                .itens(new ArrayList<>())
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
        
        // Adicionar itens válidos ao pedido
        List<ItemPedido> itens = new ArrayList<>();
        
        // Criar produtos fictícios para os itens
        Produtos produto1 = ProdutoFixture.createValidProduto();
        produto1.setId(UUID.randomUUID());
        produto1.setPreco(new BigDecimal("899.99"));
        
        Produtos produto2 = ProdutoFixture.createValidProduto();
        produto2.setId(UUID.randomUUID());
        produto2.setPreco(new BigDecimal("899.99"));
        
        // Criar itens do pedido
        ItemPedido item1 = createItemPedidoWithParams(pedido, produto1, 1, produto1.getPreco());
        ItemPedido item2 = createItemPedidoWithParams(pedido, produto2, 1, produto2.getPreco());
        
        itens.add(item1);
        itens.add(item2);
        
        pedido.setItens(itens);
        return pedido;
    }

    public static Pedido createPedidoWithParams(Cliente cliente, PedidoStatus status, BigDecimal valorTotal) {
        return Pedido.builder()
                .cliente(cliente)
                .status(status)
                .valorTotal(valorTotal)
                .itens(new ArrayList<>())
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Pedido createPedidoWithData(Cliente cliente, LocalDateTime data) {
        return Pedido.builder()
                .cliente(cliente)
                .status(PedidoStatus.PENDENTE)
                .valorTotal(new BigDecimal("999.99"))
                .itens(new ArrayList<>())
                .dataCadastro(data)
                .dataAtualizacao(data)
                .build();
    }

    public static Pedido createPedidoWithAllParams(UUID id, Cliente cliente, PedidoStatus status, BigDecimal valorTotal, LocalDateTime data) {
        return Pedido.builder()
                .id(id)
                .cliente(cliente)
                .status(status)
                .valorTotal(valorTotal)
                .itens(new ArrayList<>())
                .dataCadastro(data)
                .dataAtualizacao(data)
                .build();
    }

    public static ItemPedido createValidItemPedido(Pedido pedido, Produtos produto) {
        return createItemPedidoWithParams(pedido, produto, 2, produto.getPreco());
    }

    public static ItemPedido createItemPedidoWithQuantidade(Pedido pedido, Produtos produto, Integer quantidade) {
        return createItemPedidoWithParams(pedido, produto, quantidade, produto.getPreco());
    }

    public static ItemPedido createItemPedidoWithParams(Pedido pedido, Produtos produto, Integer quantidade, BigDecimal precoUnitario) {
        return ItemPedido.builder()
                .pedido(pedido)
                .produtos(produto)
                .quantidade(quantidade)
                .precoUnitario(precoUnitario)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static PedidoRequestDTO createValidPedidoRequestDTO(UUID clienteId) {
        List<ItemPedidoRequestDTO> itens = List.of(
                new ItemPedidoRequestDTO(UUID.randomUUID(), 2)
        );
        return new PedidoRequestDTO(clienteId, itens);
    }

    public static PedidoRequestDTO createValidPedidoRequestDTO(UUID clienteId, UUID produtoId) {
        List<ItemPedidoRequestDTO> itens = List.of(
                new ItemPedidoRequestDTO(produtoId, 2)
        );
        return new PedidoRequestDTO(clienteId, itens);
    }

    public static ItemPedido createItemPedido(Pedido pedido, Produtos produto, int quantidade) {
        return createItemPedidoWithParams(pedido, produto, quantidade, produto.getPreco());
    }

    public static ItemPedidoRequestDTO createValidItemPedidoRequestDTO(UUID produtoId) {
        return new ItemPedidoRequestDTO(produtoId, 2);
    }

    public static ItemPedidoRequestDTO createItemPedidoRequestDTOWithQuantidade(UUID produtoId, Integer quantidade) {
        return new ItemPedidoRequestDTO(produtoId, quantidade);
    }

    /**
     * Cria um pedido SEM itens para testes específicos que necessitam dessa condição
     */
    public static Pedido createPedidoWithoutItems(Cliente cliente) {
        return Pedido.builder()
                .cliente(cliente)
                .status(PedidoStatus.PENDENTE)
                .valorTotal(new BigDecimal("100.00"))
                .itens(new ArrayList<>())
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }
}