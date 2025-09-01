package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.exception.EstoqueInsuficienteException;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EstoqueServiceTest extends BaseUnitTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private EstoqueService estoqueService;

    private UUID produtoId;
    private Produtos produto;
    private ItemPedidoRequestDTO itemDto;

    @BeforeEach
    void setUp() {
        produtoId = UUID.randomUUID();
        produto = ProdutoFixture.createProdutoWithEstoque(50L);
        produto.setId(produtoId);
        
        itemDto = new ItemPedidoRequestDTO(produtoId, 10);
    }

    @Test
    @DisplayName("Deve validar estoque disponível com sucesso")
    void deveValidarEstoqueDisponivelComSucesso() {
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        assertDoesNotThrow(() -> estoqueService.validarEstoqueDisponivel(List.of(itemDto)));

        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        ItemPedidoRequestDTO itemComQuantidadeExcessiva = new ItemPedidoRequestDTO(produtoId, 100);
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        EstoqueInsuficienteException exception = assertThrows(EstoqueInsuficienteException.class,
                () -> estoqueService.validarEstoqueDisponivel(List.of(itemComQuantidadeExcessiva)));

        assertEquals(produto.getNome(), exception.getNomeProduto());
        assertEquals(produtoId, exception.getProdutoId());
        assertEquals(produto.getQuantidadeEstoque(), exception.getEstoqueDisponivel());
        assertEquals(100, exception.getQuantidadeSolicitada());
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> estoqueService.validarEstoqueDisponivel(List.of(itemDto)));

        assertEquals("Produto", exception.getEntityType());
        assertEquals(produtoId, exception.getEntityId());
    }

    @Test
    @DisplayName("Deve verificar estoque para pagamento com sucesso")
    void deveVerificarEstoqueParaPagamentoComSucesso() {
        ItemPedido itemPedido = ItemPedido.builder()
                .produtos(produto)
                .quantidade(10)
                .build();

        assertDoesNotThrow(() -> estoqueService.verificarEstoqueParaPagamento(List.of(itemPedido)));
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque insuficiente para pagamento")
    void deveLancarExcecaoQuandoEstoqueInsuficienteParaPagamento() {
        Produtos produtoSemEstoque = ProdutoFixture.createProdutoWithEstoque(5L);
        ItemPedido itemPedido = ItemPedido.builder()
                .produtos(produtoSemEstoque)
                .quantidade(10)
                .build();

        EstoqueInsuficienteException exception = assertThrows(EstoqueInsuficienteException.class,
                () -> estoqueService.verificarEstoqueParaPagamento(List.of(itemPedido)));

        assertEquals(produtoSemEstoque.getNome(), exception.getNomeProduto());
    }

    @Test
    @DisplayName("Deve atualizar estoque após venda")
    void deveAtualizarEstoqueAposVenda() {
        ItemPedido itemPedido = ItemPedido.builder()
                .produtos(produto)
                .quantidade(10)
                .build();

        estoqueService.atualizarEstoqueAposVenda(List.of(itemPedido));

        assertEquals(40L, produto.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve verificar disponibilidade de estoque")
    void deveVerificarDisponibilidadeDeEstoque() {
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        boolean disponivel = estoqueService.verificarDisponibilidade(produtoId, 30);
        boolean indisponivel = estoqueService.verificarDisponibilidade(produtoId, 100);

        assertTrue(disponivel);
        assertFalse(indisponivel);
        verify(produtoRepository, times(2)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve validar múltiplos itens com diferentes quantidades")
    void deveValidarMultiplosItensComDiferentesQuantidades() {
        UUID produto2Id = UUID.randomUUID();
        Produtos produto2 = ProdutoFixture.createProdutoWithEstoque(20L);
        produto2.setId(produto2Id);

        ItemPedidoRequestDTO item1 = new ItemPedidoRequestDTO(produtoId, 25);
        ItemPedidoRequestDTO item2 = new ItemPedidoRequestDTO(produto2Id, 15);

        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(produtoRepository.findById(produto2Id)).thenReturn(Optional.of(produto2));

        assertDoesNotThrow(() -> estoqueService.validarEstoqueDisponivel(List.of(item1, item2)));

        verify(produtoRepository, times(1)).findById(produtoId);
        verify(produtoRepository, times(1)).findById(produto2Id);
    }

    @Test
    @DisplayName("Deve atualizar estoque de múltiplos produtos")
    void deveAtualizarEstoqueDeMultiplosProdutos() {
        Produtos produto2 = ProdutoFixture.createProdutoWithEstoque(30L);
        
        ItemPedido item1 = ItemPedido.builder()
                .produtos(produto)
                .quantidade(15)
                .build();
        
        ItemPedido item2 = ItemPedido.builder()
                .produtos(produto2)
                .quantidade(10)
                .build();

        estoqueService.atualizarEstoqueAposVenda(List.of(item1, item2));

        assertEquals(35L, produto.getQuantidadeEstoque());
        assertEquals(20L, produto2.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).saveAll(anyList());
    }
}
