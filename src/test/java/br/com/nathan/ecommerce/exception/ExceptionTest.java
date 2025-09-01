package br.com.nathan.ecommerce.exception;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest extends BaseUnitTest {

    @Test
    @DisplayName("Deve criar EstoqueInsuficienteException com todos os parâmetros")
    void deveCriarEstoqueInsuficienteExceptionComTodosParametros() {
        UUID produtoId = UUID.randomUUID();
        String nomeProduto = "Smartphone";
        Long estoqueDisponivel = 5L;
        Integer quantidadeSolicitada = 10;

        EstoqueInsuficienteException exception = new EstoqueInsuficienteException(
                produtoId, nomeProduto, estoqueDisponivel, quantidadeSolicitada);

        assertEquals(produtoId, exception.getProdutoId());
        assertEquals(nomeProduto, exception.getNomeProduto());
        assertEquals(estoqueDisponivel, exception.getEstoqueDisponivel());
        assertEquals(quantidadeSolicitada, exception.getQuantidadeSolicitada());
        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        assertTrue(exception.getMessage().contains(nomeProduto));
        assertTrue(exception.getMessage().contains("5"));
        assertTrue(exception.getMessage().contains("10"));
    }

    @Test
    @DisplayName("Deve criar EstoqueInsuficienteException apenas com nome do produto")
    void deveCriarEstoqueInsuficienteExceptionApenasComNomeProduto() {
        String nomeProduto = "Notebook";

        EstoqueInsuficienteException exception = new EstoqueInsuficienteException(nomeProduto);

        assertNull(exception.getProdutoId());
        assertEquals(nomeProduto, exception.getNomeProduto());
        assertNull(exception.getEstoqueDisponivel());
        assertNull(exception.getQuantidadeSolicitada());
        assertTrue(exception.getMessage().contains("Estoque insuficiente"));
        assertTrue(exception.getMessage().contains(nomeProduto));
    }

    @Test
    @DisplayName("Deve criar EntityNotFoundException com UUID")
    void deveCriarEntityNotFoundExceptionComUUID() {
        String entityType = "Produto";
        UUID entityId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(entityType, entityId);

        assertEquals(entityType, exception.getEntityType());
        assertEquals(entityId, exception.getEntityId());
        assertTrue(exception.getMessage().contains(entityType));
        assertTrue(exception.getMessage().contains(entityId.toString()));
    }

    @Test
    @DisplayName("Deve criar EntityNotFoundException com identificador string")
    void deveCriarEntityNotFoundExceptionComIdentificadorString() {
        String entityType = "Cliente";
        String identifier = "cliente@email.com";

        EntityNotFoundException exception = new EntityNotFoundException(entityType, identifier);

        assertEquals(entityType, exception.getEntityType());
        assertNull(exception.getEntityId());
        assertTrue(exception.getMessage().contains(entityType));
        assertTrue(exception.getMessage().contains(identifier));
    }

    @Test
    @DisplayName("Deve criar PagamentoException com pedido ID")
    void deveCriarPagamentoExceptionComPedidoId() {
        UUID pedidoId = UUID.randomUUID();
        String motivo = "Cartão inválido";

        PagamentoException exception = new PagamentoException(pedidoId, motivo);

        assertEquals(pedidoId, exception.getPedidoId());
        assertEquals(motivo, exception.getMotivo());
        assertTrue(exception.getMessage().contains(pedidoId.toString()));
        assertTrue(exception.getMessage().contains(motivo));
    }

    @Test
    @DisplayName("Deve criar PagamentoException apenas com motivo")
    void deveCriarPagamentoExceptionApenasComMotivo() {
        String motivo = "Saldo insuficiente";

        PagamentoException exception = new PagamentoException(motivo);

        assertNull(exception.getPedidoId());
        assertEquals(motivo, exception.getMotivo());
        assertTrue(exception.getMessage().contains(motivo));
    }

    @Test
    @DisplayName("Deve criar PedidoInvalidoException com todos os parâmetros")
    void deveCriarPedidoInvalidoExceptionComTodosParametros() {
        UUID pedidoId = UUID.randomUUID();
        PedidoStatus statusAtual = PedidoStatus.APROVADO;
        String operacao = "cancelar";

        PedidoInvalidoException exception = new PedidoInvalidoException(pedidoId, statusAtual, operacao);

        assertEquals(pedidoId, exception.getPedidoId());
        assertEquals(statusAtual, exception.getStatusAtual());
        assertEquals(operacao, exception.getOperacao());
        assertTrue(exception.getMessage().contains(pedidoId.toString()));
        assertTrue(exception.getMessage().contains(statusAtual.toString()));
        assertTrue(exception.getMessage().contains(operacao));
    }

    @Test
    @DisplayName("Deve criar PedidoInvalidoException apenas com mensagem")
    void deveCriarPedidoInvalidoExceptionApenasComMensagem() {
        String mensagem = "Operação não permitida";

        PedidoInvalidoException exception = new PedidoInvalidoException(mensagem);

        assertNull(exception.getPedidoId());
        assertNull(exception.getStatusAtual());
        assertNull(exception.getOperacao());
        assertEquals(mensagem, exception.getMessage());
    }

    @Test
    @DisplayName("Deve criar ValidationException com todos os parâmetros")
    void deveCriarValidationExceptionComTodosParametros() {
        String campo = "email";
        String valorInvalido = "email-invalido";
        String motivo = "formato inválido";

        ValidationException exception = new ValidationException(campo, valorInvalido, motivo);

        assertEquals(campo, exception.getCampo());
        assertEquals(valorInvalido, exception.getValorInvalido());
        assertTrue(exception.getMessage().contains(campo));
        assertTrue(exception.getMessage().contains(valorInvalido));
        assertTrue(exception.getMessage().contains(motivo));
    }

    @Test
    @DisplayName("Deve criar ValidationException apenas com mensagem")
    void deveCriarValidationExceptionApenasComMensagem() {
        String mensagem = "Dados inválidos";

        ValidationException exception = new ValidationException(mensagem);

        assertNull(exception.getCampo());
        assertNull(exception.getValorInvalido());
        assertEquals(mensagem, exception.getMessage());
    }

    @Test
    @DisplayName("Deve verificar herança de BusinessException")
    void deveVerificarHerancaDeBusinessException() {
        EstoqueInsuficienteException estoqueException = new EstoqueInsuficienteException("Produto");
        EntityNotFoundException entityException = new EntityNotFoundException("Produto", UUID.randomUUID());
        PagamentoException pagamentoException = new PagamentoException("Erro");
        PedidoInvalidoException pedidoException = new PedidoInvalidoException("Erro");
        ValidationException validationException = new ValidationException("Erro");

        assertTrue(estoqueException instanceof BusinessException);
        assertTrue(entityException instanceof BusinessException);
        assertTrue(pagamentoException instanceof BusinessException);
        assertTrue(pedidoException instanceof BusinessException);
        assertTrue(validationException instanceof BusinessException);

        assertTrue(estoqueException instanceof RuntimeException);
        assertTrue(entityException instanceof RuntimeException);
        assertTrue(pagamentoException instanceof RuntimeException);
        assertTrue(pedidoException instanceof RuntimeException);
        assertTrue(validationException instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve manter informações detalhadas para debugging")
    void deveManterInformacoesDetalhadasParaDebugging() {
        UUID produtoId = UUID.randomUUID();
        UUID pedidoId = UUID.randomUUID();
        
        EstoqueInsuficienteException estoqueException = new EstoqueInsuficienteException(
                produtoId, "Produto Teste", 3L, 5);
        
        PedidoInvalidoException pedidoException = new PedidoInvalidoException(
                pedidoId, PedidoStatus.CANCELADO, "processar pagamento");

        // Verificar se todas as informações estão preservadas
        assertNotNull(estoqueException.getProdutoId());
        assertNotNull(estoqueException.getNomeProduto());
        assertNotNull(estoqueException.getEstoqueDisponivel());
        assertNotNull(estoqueException.getQuantidadeSolicitada());

        assertNotNull(pedidoException.getPedidoId());
        assertNotNull(pedidoException.getStatusAtual());
        assertNotNull(pedidoException.getOperacao());
    }

    @Test
    @DisplayName("Deve criar BusinessException base com mensagem")
    void deveCriarBusinessExceptionBaseComMensagem() {
        String mensagem = "Erro de negócio";
        
        BusinessException exception = new BusinessException(mensagem) {};

        assertEquals(mensagem, exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Deve criar BusinessException base com causa")
    void deveCriarBusinessExceptionBaseComCausa() {
        String mensagem = "Erro de negócio";
        Exception causa = new Exception("Causa original");
        
        BusinessException exception = new BusinessException(mensagem, causa) {};

        assertEquals(mensagem, exception.getMessage());
        assertEquals(causa, exception.getCause());
    }
}
