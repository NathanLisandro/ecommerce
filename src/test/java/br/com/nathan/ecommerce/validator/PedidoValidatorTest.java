package br.com.nathan.ecommerce.validator;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.Cliente;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import br.com.nathan.ecommerce.exception.PedidoInvalidoException;
import br.com.nathan.ecommerce.exception.ValidationException;
import br.com.nathan.ecommerce.fixtures.ClienteFixture;
import br.com.nathan.ecommerce.fixtures.PedidoFixture;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class PedidoValidatorTest extends BaseUnitTest {

    @InjectMocks
    private PedidoValidator pedidoValidator;

    private Cliente cliente;
    private Pedido pedido;
    private PedidoRequestDTO pedidoRequestDTO;
    private List<ItemPedidoRequestDTO> itensValidos;

    @BeforeEach
    void setUp() {
        cliente = ClienteFixture.createValidCliente();
        pedido = PedidoFixture.createValidPedido(cliente);
        pedido.setId(UUID.randomUUID());
        pedido.setStatus(PedidoStatus.PENDENTE);
        pedido.setValorTotal(new BigDecimal("100.00"));

        itensValidos = List.of(
                new ItemPedidoRequestDTO(UUID.randomUUID(), 2),
                new ItemPedidoRequestDTO(UUID.randomUUID(), 1)
        );

        pedidoRequestDTO = new PedidoRequestDTO(cliente.getId(), itensValidos);
    }

    @Test
    @DisplayName("Deve validar criação de pedido com sucesso")
    void deveValidarCriacaoDePedidoComSucesso() {
        assertDoesNotThrow(() -> pedidoValidator.validarCriacaoPedido(pedidoRequestDTO, cliente));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente é nulo")
    void deveLancarExcecaoQuandoClienteNulo() {
        ValidationException exception = assertThrows(ValidationException.class,
                () -> pedidoValidator.validarCriacaoPedido(pedidoRequestDTO, null));

        assertTrue(exception.getMessage().contains("Cliente é obrigatório"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não tem nome")
    void deveLancarExcecaoQuandoClienteNaoTemNome() {
        Cliente clienteSemNome = ClienteFixture.createValidCliente();
        clienteSemNome.setNome(null);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> pedidoValidator.validarCriacaoPedido(pedidoRequestDTO, clienteSemNome));

        assertTrue(exception.getMessage().contains("Cliente deve ter um nome válido"));
        assertEquals("nome", exception.getCampo());
    }

    @Test
    @DisplayName("Deve lançar exceção quando cliente não tem email")
    void deveLancarExcecaoQuandoClienteNaoTemEmail() {
        Cliente clienteSemEmail = ClienteFixture.createValidCliente();
        clienteSemEmail.setEmail("");

        ValidationException exception = assertThrows(ValidationException.class,
                () -> pedidoValidator.validarCriacaoPedido(pedidoRequestDTO, clienteSemEmail));

        assertTrue(exception.getMessage().contains("Cliente deve ter um email válido"));
        assertEquals("email", exception.getCampo());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não tem itens")
    void deveLancarExcecaoQuandoPedidoNaoTemItens() {
        PedidoRequestDTO pedidoSemItens = new PedidoRequestDTO(cliente.getId(), Collections.emptyList());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> pedidoValidator.validarCriacaoPedido(pedidoSemItens, cliente));

        assertTrue(exception.getMessage().contains("Pedido deve conter pelo menos um item"));
        assertEquals("itens", exception.getCampo());
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido tem muitos itens")
    void deveLancarExcecaoQuandoPedidoTemMuitosItens() {
        List<ItemPedidoRequestDTO> muitosItens = IntStream.range(0, 51)
                .mapToObj(i -> new ItemPedidoRequestDTO(UUID.randomUUID(), 1))
                .toList();

        PedidoRequestDTO pedidoComMuitosItens = new PedidoRequestDTO(cliente.getId(), muitosItens);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> pedidoValidator.validarCriacaoPedido(pedidoComMuitosItens, cliente));

        assertTrue(exception.getMessage().contains("Pedido não pode conter mais de 50 itens"));
        assertEquals("itens", exception.getCampo());
    }

    @Test
    @DisplayName("Deve validar processamento de pagamento com sucesso")
    void deveValidarProcessamentoDePagamentoComSucesso() {
        // Garantir que o pedido tem itens
        pedido.getItens().add(PedidoFixture.createValidItemPedido(pedido, ProdutoFixture.createValidProduto()));
        
        assertDoesNotThrow(() -> pedidoValidator.validarProcessamentoPagamento(pedido));
    }

    @Test
    @DisplayName("Deve lançar exceção quando pedido não está pendente para pagamento")
    void deveLancarExcecaoQuandoPedidoNaoEstaPendenteParaPagamento() {
        pedido.setStatus(PedidoStatus.APROVADO);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> pedidoValidator.validarProcessamentoPagamento(pedido));

        assertTrue(exception.getMessage().contains("Pedido não está em status PENDENTE"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor do pedido é inválido")
    void deveLancarExcecaoQuandoValorDoPedidoInvalido() {
        // Garantir que o pedido tem itens para não falhar na validação anterior
        pedido.getItens().add(PedidoFixture.createValidItemPedido(pedido, ProdutoFixture.createValidProduto()));
        pedido.setValorTotal(BigDecimal.ZERO);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> pedidoValidator.validarProcessamentoPagamento(pedido));

        assertTrue(exception.getMessage().contains("Valor do pedido deve ser maior que zero"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor do pedido excede limite")
    void deveLancarExcecaoQuandoValorDoPedidoExcedeLimite() {
        // Garantir que o pedido tem itens para não falhar na validação anterior
        pedido.getItens().add(PedidoFixture.createValidItemPedido(pedido, ProdutoFixture.createValidProduto()));
        pedido.setValorTotal(new BigDecimal("150000.00"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> pedidoValidator.validarProcessamentoPagamento(pedido));

        assertTrue(exception.getMessage().contains("excede o limite máximo"));
    }

    @Test
    @DisplayName("Deve validar cancelamento de pedido com sucesso")
    void deveValidarCancelamentoDePedidoComSucesso() {
        assertDoesNotThrow(() -> pedidoValidator.validarCancelamentoPedido(pedido));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar cancelar pedido aprovado")
    void deveLancarExcecaoAoTentarCancelarPedidoAprovado() {
        pedido.setStatus(PedidoStatus.APROVADO);

        PedidoInvalidoException exception = assertThrows(PedidoInvalidoException.class,
                () -> pedidoValidator.validarCancelamentoPedido(pedido));

        assertEquals(pedido.getId(), exception.getPedidoId());
        assertEquals(PedidoStatus.APROVADO, exception.getStatusAtual());
        assertEquals("cancelar", exception.getOperacao());
    }

    @Test
    @DisplayName("Deve validar parâmetros de relatório com sucesso")
    void deveValidarParametrosDeRelatorioComSucesso() {
        assertDoesNotThrow(() -> pedidoValidator.validarParametrosRelatorio(2024, 6));
    }

    @Test
    @DisplayName("Deve lançar exceção quando mês é inválido")
    void deveLancarExcecaoQuandoMesInvalido() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> pedidoValidator.validarParametrosRelatorio(2024, 0));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> pedidoValidator.validarParametrosRelatorio(2024, 13));

        assertTrue(exception1.getMessage().contains("Mês deve estar entre 1 e 12"));
        assertTrue(exception2.getMessage().contains("Mês deve estar entre 1 e 12"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando ano é inválido")
    void deveLancarExcecaoQuandoAnoInvalido() {
        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> pedidoValidator.validarParametrosRelatorio(2019, 6));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> pedidoValidator.validarParametrosRelatorio(2030, 6));

        assertTrue(exception1.getMessage().contains("Ano deve estar entre 2020"));
        assertTrue(exception2.getMessage().contains("Ano deve estar entre 2020"));
    }

    @Test
    @DisplayName("Deve validar quantidade de item válida")
    void deveValidarQuantidadeDeItemValida() {
        ItemPedidoRequestDTO itemValido = new ItemPedidoRequestDTO(UUID.randomUUID(), 5);
        PedidoRequestDTO pedidoComItemValido = new PedidoRequestDTO(cliente.getId(), List.of(itemValido));

        assertDoesNotThrow(() -> pedidoValidator.validarCriacaoPedido(pedidoComItemValido, cliente));
    }

    @Test
    @DisplayName("Deve validar pedido com valor no limite máximo")
    void deveValidarPedidoComValorNoLimiteMaximo() {
        // Garantir que o pedido tem itens para não falhar na validação anterior
        pedido.getItens().add(PedidoFixture.createValidItemPedido(pedido, ProdutoFixture.createValidProduto()));
        pedido.setValorTotal(new BigDecimal("100000.00"));

        assertDoesNotThrow(() -> pedidoValidator.validarProcessamentoPagamento(pedido));
    }

    @Test
    @DisplayName("Deve validar pedido sem itens para processamento")
    void deveValidarPedidoSemItensParaProcessamento() {
        pedido.setItens(new ArrayList<>());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> pedidoValidator.validarProcessamentoPagamento(pedido));

        assertTrue(exception.getMessage().contains("Pedido deve conter itens"));
    }

    @Test
    @DisplayName("Deve validar pedido com itens nulos para processamento")
    void deveValidarPedidoComItensNulosParaProcessamento() {
        pedido.setItens(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> pedidoValidator.validarProcessamentoPagamento(pedido));

        assertTrue(exception.getMessage().contains("Pedido deve conter itens"));
    }
}
