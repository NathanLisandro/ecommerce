package br.com.nathan.ecommerce.validator;

import br.com.nathan.ecommerce.domain.Cliente;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import br.com.nathan.ecommerce.exception.PedidoInvalidoException;
import br.com.nathan.ecommerce.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class PedidoValidator {

    /**
     * Valida se um pedido pode ser criado
     */
    public void validarCriacaoPedido(PedidoRequestDTO dto, Cliente cliente) {
        log.debug("Validando criação de pedido para cliente: {}", 
                cliente != null ? cliente.getNome() : "null");
        
        validarCliente(cliente);
        validarItensPedido(dto.itens());
        
        log.debug("Validação de criação de pedido aprovada");
    }

    /**
     * Valida se um pedido pode ser processado para pagamento
     */
    public void validarProcessamentoPagamento(Pedido pedido) {
        log.debug("Validando processamento de pagamento para pedido: {}", pedido.getId());
        
        validarPedidoExistente(pedido);
        validarStatusPendente(pedido);
        validarItensPedidoExistentes(pedido.getItens());
        validarValorPedido(pedido.getValorTotal());
        
        log.debug("Validação de processamento de pagamento aprovada");
    }

    /**
     * Valida se um pedido pode ser cancelado
     */
    public void validarCancelamentoPedido(Pedido pedido) {
        log.debug("Validando cancelamento de pedido: {}", pedido.getId());
        
        validarPedidoExistente(pedido);
        
        if (pedido.getStatus() == PedidoStatus.APROVADO) {
            log.error("Tentativa de cancelar pedido já aprovado: {}", pedido.getId());
            throw new PedidoInvalidoException(pedido.getId(), pedido.getStatus(), "cancelar");
        }
        
        log.debug("Validação de cancelamento de pedido aprovada");
    }

    /**
     * Valida dados do cliente
     */
    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new ValidationException("Cliente é obrigatório");
        }
        
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new ValidationException("nome", cliente.getNome(), "Cliente deve ter um nome válido");
        }
        
        if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
            throw new ValidationException("email", cliente.getEmail(), "Cliente deve ter um email válido");
        }
    }

    /**
     * Valida lista de itens do pedido
     */
    private void validarItensPedido(List<ItemPedidoRequestDTO> itens) {
        if (CollectionUtils.isEmpty(itens)) {
            throw new ValidationException("itens", itens, "Pedido deve conter pelo menos um item");
        }
        
        if (itens.size() > 50) {
            throw new ValidationException("itens", itens.size(), "Pedido não pode conter mais de 50 itens");
        }
        
        itens.forEach(this::validarItemPedido);
    }

    /**
     * Valida um item individual do pedido
     */
    private void validarItemPedido(ItemPedidoRequestDTO item) {
        if (item.produtoId() == null) {
            throw new IllegalArgumentException("Item do pedido deve ter um produto válido");
        }
        
        if (item.quantidade() == null || item.quantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
        }
        
        if (item.quantidade() > 1000) {
            throw new IllegalArgumentException("Quantidade máxima por item é 1000 unidades");
        }
    }

    /**
     * Valida se o pedido existe e está em estado válido
     */
    private void validarPedidoExistente(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo");
        }
        
        if (pedido.getId() == null) {
            throw new IllegalArgumentException("Pedido deve ter um ID válido");
        }
    }

    /**
     * Valida se o pedido está em status PENDENTE
     */
    private void validarStatusPendente(Pedido pedido) {
        if (pedido.getStatus() != PedidoStatus.PENDENTE) {
            log.error("Tentativa de processar pagamento para pedido em status inválido: {} - {}", 
                    pedido.getId(), pedido.getStatus());
            throw new IllegalStateException(
                String.format("Pedido não está em status PENDENTE. Status atual: %s", pedido.getStatus())
            );
        }
    }

    /**
     * Valida se o pedido possui itens
     */
    private void validarItensPedidoExistentes(List<ItemPedido> itens) {
        if (CollectionUtils.isEmpty(itens)) {
            throw new IllegalStateException("Pedido deve conter itens para ser processado");
        }
    }

    /**
     * Valida o valor total do pedido
     */
    private void validarValorPedido(BigDecimal valorTotal) {
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Valor do pedido deve ser maior que zero");
        }
        
        // Valor máximo por pedido: R$ 100.000
        BigDecimal valorMaximo = new BigDecimal("100000.00");
        if (valorTotal.compareTo(valorMaximo) > 0) {
            throw new IllegalStateException(
                String.format("Valor do pedido (R$ %s) excede o limite máximo de R$ %s", 
                        valorTotal, valorMaximo)
            );
        }
    }

    /**
     * Valida parâmetros de relatório mensal
     */
    public void validarParametrosRelatorio(int ano, int mes) {
        log.debug("Validando parâmetros de relatório: {}/{}", mes, ano);
        
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("Mês deve estar entre 1 e 12");
        }
        
        int anoAtual = java.time.LocalDate.now().getYear();
        if (ano < 2020 || ano > anoAtual + 1) {
            throw new IllegalArgumentException(
                String.format("Ano deve estar entre 2020 e %d", anoAtual + 1)
            );
        }
        
        log.debug("Parâmetros de relatório validados com sucesso");
    }
}
