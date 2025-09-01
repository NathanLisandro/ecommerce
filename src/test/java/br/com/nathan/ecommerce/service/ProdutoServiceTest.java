package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.dto.ProdutoResponseDTO;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import br.com.nathan.ecommerce.validator.ProdutoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProdutoServiceTest extends BaseUnitTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoValidator produtoValidator;

    @InjectMocks
    private ProdutoService produtoService;

    private Produtos produto;
    private ProdutoRequestDTO produtoRequestDTO;
    private UUID produtoId;

    @BeforeEach
    void setUp() {
        produtoId = UUID.randomUUID();
        produto = ProdutoFixture.createProdutoWithId(produtoId);
        produtoRequestDTO = ProdutoFixture.createValidProdutoRequestDTO();
    }

    @Test
    @DisplayName("Deve criar produto com sucesso")
    void deveCriarProdutoComSucesso() {
        doNothing().when(produtoValidator).validarDadosProduto(any());
        when(produtoRepository.save(any(Produtos.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.criarProduto(produtoRequestDTO);

        assertNotNull(resultado);
        assertEquals(produto.getNome(), resultado.nome());
        assertEquals(produto.getDescricao(), resultado.descricao());
        assertEquals(produto.getPreco(), resultado.preco());
        assertEquals(produto.getCategoria(), resultado.categoria());
        assertEquals(produto.getQuantidadeEstoque(), resultado.quantidadeEstoque());
        
        verify(produtoValidator, times(1)).validarDadosProduto(any());
        verify(produtoRepository, times(1)).save(any(Produtos.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID com sucesso")
    void deveBuscarProdutoPorIdComSucesso() {
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        ProdutoResponseDTO resultado = produtoService.buscarPorId(produtoId);

        assertNotNull(resultado);
        assertEquals(produto.getId(), resultado.id());
        assertEquals(produto.getNome(), resultado.nome());
        
        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado")
    void deveLancarExcecaoQuandoProdutoNaoEncontrado() {
        UUID idInexistente = UUID.randomUUID();
        when(produtoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> produtoService.buscarPorId(idInexistente));
        
        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        assertEquals("Produto", exception.getEntityType());
        assertEquals(idInexistente, exception.getEntityId());
        verify(produtoRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve listar todos os produtos com paginação")
    void deveListarTodosOsProdutosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produtos> produtosPage = new PageImpl<>(List.of(produto));
        when(produtoRepository.findAll(pageable)).thenReturn(produtosPage);

        Page<ProdutoResponseDTO> resultado = produtoService.listarTodos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals(produto.getNome(), resultado.getContent().get(0).nome());
        
        verify(produtoRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar produtos por nome")
    void deveBuscarProdutosPorNome() {
        String nome = "Smartphone";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Produtos> produtosPage = new PageImpl<>(List.of(produto));
        doNothing().when(produtoValidator).validarParametrosBusca(eq(nome), eq(null));
        when(produtoRepository.findByNomeContainingIgnoreCase(nome, pageable)).thenReturn(produtosPage);

        Page<ProdutoResponseDTO> resultado = produtoService.buscarPorNome(nome, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertTrue(resultado.getContent().get(0).nome().contains("Smartphone"));
        
        verify(produtoValidator, times(1)).validarParametrosBusca(eq(nome), eq(null));
        verify(produtoRepository, times(1)).findByNomeContainingIgnoreCase(nome, pageable);
    }

    @Test
    @DisplayName("Deve atualizar produto com sucesso")
    void deveAtualizarProdutoComSucesso() {
        ProdutoRequestDTO novosDados = ProdutoFixture.createProdutoRequestDTOWithNome("Produto Atualizado");
        doNothing().when(produtoValidator).validarDadosProduto(any());
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produtos.class))).thenReturn(produto);

        ProdutoResponseDTO resultado = produtoService.atualizarProduto(produtoId, novosDados);

        assertNotNull(resultado);
        verify(produtoValidator, times(1)).validarDadosProduto(any());
        verify(produtoRepository, times(1)).findById(produtoId);
        verify(produtoRepository, times(1)).save(produto);
    }

    @Test
    @DisplayName("Deve deletar produto com sucesso")
    void deveDeletarProdutoComSucesso() {
        when(produtoRepository.existsById(produtoId)).thenReturn(true);

        produtoService.deletarProduto(produtoId);

        verify(produtoRepository, times(1)).existsById(produtoId);
        verify(produtoRepository, times(1)).deleteById(produtoId);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar produto inexistente")
    void deveLancarExcecaoAoDeletarProdutoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(produtoRepository.existsById(idInexistente)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> produtoService.deletarProduto(idInexistente));
        
        assertTrue(exception.getMessage().contains("Produto não encontrado"));
        assertEquals("Produto", exception.getEntityType());
        assertEquals(idInexistente, exception.getEntityId());
        verify(produtoRepository, times(1)).existsById(idInexistente);
        verify(produtoRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Deve verificar estoque disponível")
    void deveVerificarEstoqueDisponivel() {
        Integer quantidadeSolicitada = 10;
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        boolean temEstoque = produtoService.verificarEstoque(produtoId, quantidadeSolicitada);

        assertTrue(temEstoque);
        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve verificar estoque insuficiente")
    void deveVerificarEstoqueInsuficiente() {
        Integer quantidadeSolicitada = 100;
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));

        boolean temEstoque = produtoService.verificarEstoque(produtoId, quantidadeSolicitada);

        assertFalse(temEstoque);
        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve atualizar estoque do produto")
    void deveAtualizarEstoqueDoProduto() {
        Long novaQuantidade = 25L;
        doNothing().when(produtoValidator).validarAtualizacaoEstoque(novaQuantidade);
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        produtoService.atualizarEstoque(produtoId, novaQuantidade);

        verify(produtoValidator, times(1)).validarAtualizacaoEstoque(novaQuantidade);
        verify(produtoRepository, times(1)).findById(produtoId);
        verify(produtoRepository, times(1)).save(produto);
    }
}