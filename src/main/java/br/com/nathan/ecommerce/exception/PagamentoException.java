package br.com.nathan.ecommerce.exception;

import java.util.UUID;

/**
 * Exceção para erros relacionados ao processamento de pagamento
 */
public class PagamentoException extends BusinessException {
    
    private final UUID pedidoId;
    private final String motivo;

    public PagamentoException(UUID pedidoId, String motivo) {
        super(String.format("Erro no processamento do pagamento do pedido %s: %s", pedidoId, motivo));
        this.pedidoId = pedidoId;
        this.motivo = motivo;
    }

    public PagamentoException(String motivo) {
        super(String.format("Erro no processamento do pagamento: %s", motivo));
        this.pedidoId = null;
        this.motivo = motivo;
    }

    public UUID getPedidoId() {
        return pedidoId;
    }

    public String getMotivo() {
        return motivo;
    }
}
