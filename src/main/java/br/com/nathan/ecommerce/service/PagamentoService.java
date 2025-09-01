package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import br.com.nathan.ecommerce.exception.PagamentoException;
import br.com.nathan.ecommerce.exception.PedidoInvalidoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagamentoService {

    /**
     * Processa o pagamento do pedido
     * @param pedido Pedido a ser processado
     * @return true se pagamento aprovado, false se reprovado
     */
    public boolean processarPagamento(Pedido pedido) {
        log.info("Iniciando processamento de pagamento para pedido {}", pedido.getId());
        
        if (pedido.getStatus() != PedidoStatus.PENDENTE) {
            log.error("Tentativa de processar pagamento para pedido em status inválido: {}", 
                    pedido.getStatus());
            throw new PedidoInvalidoException(
                pedido.getId(), 
                pedido.getStatus(), 
                "processar pagamento"
            );
        }

        // Validações de negócio do pagamento
        validarPagamento(pedido);

        // Simular processamento do pagamento
        boolean aprovado = simularGatewayPagamento(pedido);
        
        if (aprovado) {
            log.info("Pagamento aprovado para pedido {} no valor de R$ {}", 
                    pedido.getId(), pedido.getValorTotal());
        } else {
            log.warn("Pagamento reprovado para pedido {} no valor de R$ {}", 
                    pedido.getId(), pedido.getValorTotal());
        }
        
        return aprovado;
    }

    /**
     * Validações de negócio para o pagamento
     */
    private void validarPagamento(Pedido pedido) {
        if (pedido.getValorTotal() == null || pedido.getValorTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PagamentoException(pedido.getId(), "Valor do pedido inválido para pagamento");
        }

        if (pedido.getItens() == null || pedido.getItens().isEmpty()) {
            throw new PagamentoException(pedido.getId(), "Pedido sem itens não pode ser pago");
        }

        if (pedido.getCliente() == null) {
            throw new PagamentoException(pedido.getId(), "Pedido sem cliente não pode ser pago");
        }

        log.debug("Validações de pagamento aprovadas para pedido {}", pedido.getId());
    }

    /**
     * Simula integração com gateway de pagamento
     * Em um ambiente real, aqui seria feita a integração com serviços como:
     * - Stripe, PayPal, PagSeguro, Mercado Pago, etc.
     */
    private boolean simularGatewayPagamento(Pedido pedido) {
        log.debug("Simulando gateway de pagamento para pedido {} - valor: R$ {}", 
                pedido.getId(), pedido.getValorTotal());
        
        try {
            // Simula latência de rede
            Thread.sleep(100);
            
            // Simula 90% de aprovação
            boolean aprovado = Math.random() < 0.9;
            
            log.debug("Gateway retornou: {}", aprovado ? "APROVADO" : "REPROVADO");
            return aprovado;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Erro durante simulação de pagamento", e);
            return false;
        }
    }

    /**
     * Calcula taxas e valores adicionais do pagamento (se necessário)
     */
    public BigDecimal calcularTaxasPagamento(BigDecimal valorBase) {
        // Aqui poderia haver lógica para calcular taxas de cartão, juros, etc.
        return BigDecimal.ZERO;
    }

    /**
     * Verifica se um valor está dentro dos limites permitidos para pagamento
     */
    public boolean validarLimitePagamento(BigDecimal valor) {
        final BigDecimal LIMITE_MAXIMO = new BigDecimal("50000.00"); // R$ 50.000
        final BigDecimal LIMITE_MINIMO = new BigDecimal("0.01");     // R$ 0,01
        
        return valor.compareTo(LIMITE_MINIMO) >= 0 && valor.compareTo(LIMITE_MAXIMO) <= 0;
    }
}
