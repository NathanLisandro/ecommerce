package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.Cliente;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoResponseDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.exception.EstoqueInsuficienteException;
import br.com.nathan.ecommerce.fixtures.ClienteFixture;
import br.com.nathan.ecommerce.fixtures.PedidoFixture;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import br.com.nathan.ecommerce.repository.ClienteRepository;
import br.com.nathan.ecommerce.repository.PedidoRepository;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import br.com.nathan.ecommerce.validator.PedidoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest extends BaseUnitTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private PagamentoService pagamentoService;

    @Mock
    private PedidoValidator pedidoValidator;

    @InjectMocks
    private PedidoService pedidoService;

    // Test Data - usando fixtures para manter consistência
    private Cliente cliente;
    private Produtos produto;
    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;
    private UUID clienteId;
    private UUID produtoId;
    private UUID pedidoId;

    @BeforeEach
    void setUp() {
        // Given - Preparar dados de teste usando fixtures
        clienteId = UUID.randomUUID();
        produtoId = UUID.randomUUID();
        pedidoId = UUID.randomUUID();

        cliente = ClienteFixture.createClienteWithId(clienteId);
        produto = ProdutoFixture.createProdutoWithId(produtoId);
        produto.setQuantidadeEstoque(50L); // Estoque suficiente por padrão
        
        pedido = PedidoFixture.createPedidoWithId(pedidoId, cliente);
        
        // Criar item do pedido usando fixture
        ItemPedido itemPedido = PedidoFixture.createItemPedido(pedido, produto, 2);
        pedido.getItens().add(itemPedido);
        pedido.setValorTotal(itemPedido.getPrecoTotal());

        pedidoRequestDTO = PedidoFixture.createValidPedidoRequestDTO(clienteId, produtoId);
    }

    // ==================== TESTES DE CRIAÇÃO DE PEDIDO ====================

    @Test
    @DisplayName("Deve criar pedido com sucesso quando todos os dados são válidos")
    void deveCriarPedidoComSucesso() {
        // Given - Configurar mocks para cenário de sucesso
        configurarMocksParaCriacaoComSucesso();

        // When - Executar a criação do pedido
        PedidoResponseDTO resultado = pedidoService.criarPedido(pedidoRequestDTO);

        // Then - Verificar resultado e interações
        assertAll("Verificações do pedido criado",
            () -> assertNotNull(resultado, "Resultado não deve ser nulo"),
            () -> assertEquals(clienteId, resultado.clienteId(), "ID do cliente deve corresponder"),
            () -> assertEquals(cliente.getNome(), resultado.nomeCliente(), "Nome do cliente deve corresponder"),
            () -> assertEquals(PedidoStatus.PENDENTE, resultado.status(), "Status deve ser PENDENTE"),
            () -> assertFalse(resultado.itens().isEmpty(), "Pedido deve ter itens")
        );

        verificarInteracoesCriacaoComSucesso();
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {
        // Given - Cliente não existe
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        // When & Then - Deve lançar exceção
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.criarPedido(pedidoRequestDTO),
                "Deve lançar EntityNotFoundException quando cliente não encontrado");

        // Verificar detalhes da exceção
        assertAll("Verificações da exceção de cliente não encontrado",
            () -> assertTrue(exception.getMessage().contains("Cliente não encontrado"),
                    "Mensagem deve conter 'Cliente não encontrado'"),
            () -> assertEquals("Cliente", exception.getEntityType(),
                    "Tipo da entidade deve ser 'Cliente'"),
            () -> assertEquals(clienteId, exception.getEntityId(),
                    "ID da entidade deve corresponder ao clienteId")
        );

        // Verificar que repositório não foi chamado para salvar
        verify(clienteRepository, times(1)).findById(clienteId);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(estoqueService, never()).validarEstoqueDisponivel(any());
    }

    @Test
    @DisplayName("Deve lançar EstoqueInsuficienteException quando não há estoque suficiente")
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        // Given - Cliente existe mas estoque é insuficiente
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        doNothing().when(pedidoValidator).validarCriacaoPedido(any(), any());
        
        EstoqueInsuficienteException estoqueException = new EstoqueInsuficienteException(
            produtoId, produto.getNome(), 1L, 2);
        doThrow(estoqueException).when(estoqueService).validarEstoqueDisponivel(any());

        // When & Then - Deve lançar exceção de estoque
        EstoqueInsuficienteException exception = assertThrows(EstoqueInsuficienteException.class,
                () -> pedidoService.criarPedido(pedidoRequestDTO),
                "Deve lançar EstoqueInsuficienteException quando estoque insuficiente");

        // Verificar detalhes da exceção
        assertAll("Verificações da exceção de estoque insuficiente",
            () -> assertTrue(exception.getMessage().contains("Estoque insuficiente"),
                    "Mensagem deve conter 'Estoque insuficiente'"),
            () -> assertEquals(produto.getNome(), exception.getNomeProduto(),
                    "Nome do produto deve corresponder"),
            () -> assertEquals(produtoId, exception.getProdutoId(),
                    "ID do produto deve corresponder")
        );

        // Verificar que pedido não foi salvo
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }



    // ==================== TESTES DE BUSCA DE PEDIDO ====================

    @Test
    @DisplayName("Deve buscar pedido por ID com sucesso quando pedido existe")
    void deveBuscarPedidoPorIdComSucesso() {
        // Given - Pedido existe no repositório
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));

        // When - Buscar pedido
        PedidoResponseDTO resultado = pedidoService.buscarPorId(pedidoId);

        // Then - Verificar resultado
        assertAll("Verificações do pedido encontrado",
            () -> assertNotNull(resultado, "Resultado não deve ser nulo"),
            () -> assertEquals(pedidoId, resultado.id(), "ID deve corresponder"),
            () -> assertEquals(clienteId, resultado.clienteId(), "Cliente ID deve corresponder")
        );
        
        verify(pedidoRepository, times(1)).findById(pedidoId);
    }
    
    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando pedido não existe")
    void deveLancarExcecaoQuandoPedidoNaoEncontrado() {
        // Given - Pedido não existe
        UUID pedidoInexistente = UUID.randomUUID();
        when(pedidoRepository.findById(pedidoInexistente)).thenReturn(Optional.empty());

        // When & Then - Deve lançar exceção
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> pedidoService.buscarPorId(pedidoInexistente),
                "Deve lançar EntityNotFoundException quando pedido não encontrado");

        // Verificar detalhes da exceção
        assertAll("Verificações da exceção",
            () -> assertEquals("Pedido", exception.getEntityType()),
            () -> assertEquals(pedidoInexistente, exception.getEntityId())
        );
    }

    // ==================== TESTES DE PROCESSAMENTO DE PAGAMENTO ====================

    @Test
    @DisplayName("Deve processar pagamento com sucesso quando pagamento é aprovado")
    void deveProcessarPagamentoComSucesso() {
        // Given - Configurar mocks para pagamento aprovado
        configurarMocksParaPagamentoAprovado();

        // When - Processar pagamento
        PedidoResponseDTO resultado = pedidoService.processarPagamento(pedidoId);

        // Then - Verificar resultado
        assertNotNull(resultado, "Resultado não deve ser nulo");
        
        verificarInteracoesPagamentoAprovado();
    }

    @Test
    @DisplayName("Deve cancelar pedido automaticamente quando estoque insuficiente durante pagamento")
    void deveCancelarPedidoAutomaticamenteQuandoEstoqueInsuficienteNoPagamento() {
        // Given - Estoque se torna insuficiente durante o pagamento
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoValidator).validarProcessamentoPagamento(any());
        
        EstoqueInsuficienteException estoqueException = new EstoqueInsuficienteException(produto.getNome());
        doThrow(estoqueException).when(estoqueService).verificarEstoqueParaPagamento(any());
        
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedidoSalvo = invocation.getArgument(0);
            assertEquals(PedidoStatus.CANCELADO, pedidoSalvo.getStatus(), 
                    "Pedido deve ser cancelado quando estoque insuficiente");
            return pedidoSalvo;
        });

        // When & Then - Deve lançar exceção e cancelar pedido
        EstoqueInsuficienteException exception = assertThrows(EstoqueInsuficienteException.class,
                () -> pedidoService.processarPagamento(pedidoId),
                "Deve lançar EstoqueInsuficienteException");

        // Verificar que pedido foi cancelado mas pagamento não foi processado
        assertTrue(exception.getMessage().contains("Estoque insuficiente"),
                "Mensagem deve conter 'Estoque insuficiente'");
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(pagamentoService, never()).processarPagamento(any());
    }

    // ==================== TESTES DE CANCELAMENTO DE PEDIDO ====================

    @Test
    @DisplayName("Deve cancelar pedido com sucesso quando pedido pode ser cancelado")
    void deveCancelarPedidoComSucesso() {
        // Given - Pedido pode ser cancelado
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoValidator).validarCancelamentoPedido(any());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedidoSalvo = invocation.getArgument(0);
            pedidoSalvo.setStatus(PedidoStatus.CANCELADO);
            return pedidoSalvo;
        });

        // When - Cancelar pedido
        PedidoResponseDTO resultado = pedidoService.cancelarPedido(pedidoId);

        // Then - Verificar resultado
        assertNotNull(resultado, "Resultado não deve ser nulo");
        
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoValidator, times(1)).validarCancelamentoPedido(any());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    // ==================== TESTES DE LISTAGEM DE PEDIDOS ====================

    @Test
    @DisplayName("Deve listar pedidos do cliente com paginação quando cliente tem pedidos")
    void deveListarPedidosDoClienteComPaginacao() {
        // Given - Cliente tem pedidos
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> pedidosPage = new PageImpl<>(List.of(pedido));
        when(pedidoRepository.findByClienteId(clienteId, pageable)).thenReturn(pedidosPage);

        // When - Listar pedidos
        Page<PedidoResponseDTO> resultado = pedidoService.listarPedidosDoCliente(clienteId, pageable);

        // Then - Verificar resultado
        assertAll("Verificações da listagem de pedidos",
            () -> assertNotNull(resultado, "Resultado não deve ser nulo"),
            () -> assertEquals(1, resultado.getTotalElements(), "Deve ter 1 elemento"),
            () -> assertEquals(clienteId, resultado.getContent().get(0).clienteId(), "Cliente ID deve corresponder")
        );
        
        verify(pedidoRepository, times(1)).findByClienteId(clienteId, pageable);
    }
    
    @Test
    @DisplayName("Deve listar todos os pedidos com paginação")
    void deveListarTodosPedidosComPaginacao() {
        // Given - Há pedidos no sistema
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pedido> pedidosPage = new PageImpl<>(List.of(pedido));
        when(pedidoRepository.findAll(pageable)).thenReturn(pedidosPage);

        // When - Listar todos os pedidos
        Page<PedidoResponseDTO> resultado = pedidoService.listarTodosPedidos(pageable);

        // Then - Verificar resultado
        assertAll("Verificações da listagem de todos os pedidos",
            () -> assertNotNull(resultado, "Resultado não deve ser nulo"),
            () -> assertEquals(1, resultado.getTotalElements(), "Deve ter 1 elemento")
        );
        
        verify(pedidoRepository, times(1)).findAll(pageable);
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private void configurarMocksParaCriacaoComSucesso() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produto));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doNothing().when(pedidoValidator).validarCriacaoPedido(any(), any());
        doNothing().when(estoqueService).validarEstoqueDisponivel(any());
    }
    
    private void verificarInteracoesCriacaoComSucesso() {
        verify(clienteRepository, times(1)).findById(clienteId);
        verify(pedidoValidator, times(1)).validarCriacaoPedido(eq(pedidoRequestDTO), eq(cliente));
        verify(estoqueService, times(1)).validarEstoqueDisponivel(eq(pedidoRequestDTO.itens()));
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
    
    private void configurarMocksParaPagamentoAprovado() {
        when(pedidoRepository.findById(pedidoId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        doNothing().when(pedidoValidator).validarProcessamentoPagamento(any());
        doNothing().when(estoqueService).verificarEstoqueParaPagamento(any());
        when(pagamentoService.processarPagamento(any())).thenReturn(true);
        doNothing().when(estoqueService).atualizarEstoqueAposVenda(any());
    }
    
    private void verificarInteracoesPagamentoAprovado() {
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoValidator, times(1)).validarProcessamentoPagamento(eq(pedido));
        verify(estoqueService, times(1)).verificarEstoqueParaPagamento(eq(pedido.getItens()));
        verify(pagamentoService, times(1)).processarPagamento(eq(pedido));
        verify(estoqueService, times(1)).atualizarEstoqueAposVenda(eq(pedido.getItens()));
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }
}