package br.com.nathan.ecommerce.exception;

import br.com.nathan.ecommerce.enums.PedidoStatus;

import java.util.UUID;

/**
 * Exceção para operações inválidas em pedidos
 */
public class PedidoInvalidoException extends BusinessException {
    
    private final UUID pedidoId;
    private final PedidoStatus statusAtual;
    private final String operacao;

    public PedidoInvalidoException(UUID pedidoId, PedidoStatus statusAtual, String operacao) {
        super(String.format(
            "Não é possível %s o pedido %s. Status atual: %s", 
            operacao, pedidoId, statusAtual
        ));
        this.pedidoId = pedidoId;
        this.statusAtual = statusAtual;
        this.operacao = operacao;
    }

    public PedidoInvalidoException(String mensagem) {
        super(mensagem);
        this.pedidoId = null;
        this.statusAtual = null;
        this.operacao = null;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public PedidoStatus getStatusAtual() {
        return statusAtual;
    }

    public String getOperacao() {
        return operacao;
    }
}
