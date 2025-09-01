package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.dto.TicketMedioDTO;
import br.com.nathan.ecommerce.dto.TopClienteDTO;
import br.com.nathan.ecommerce.repository.PedidoRepository;
import br.com.nathan.ecommerce.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioService {

    private final PedidoRepository pedidoRepository;
    private final PedidoValidator pedidoValidator;

    /**
     * Obtém o ranking dos top 5 clientes que mais compraram (por valor)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "topClientes", key = "#pageable.pageSize")
    public List<TopClienteDTO> obterTop5ClientesPorCompras(Pageable pageable) {
        log.debug("Gerando relatório: Top 5 clientes por valor de compras");
        
        List<TopClienteDTO> resultado = pedidoRepository.findTop5ClientesPorValorComprado(pageable);
        
        log.info("Relatório gerado: {} clientes no ranking", resultado.size());
        resultado.forEach(cliente -> 
            log.debug("Cliente: {} - Total: R$ {} ({} pedidos)", 
                    cliente.nomeCliente(), cliente.valorTotalComprado(), cliente.totalPedidos())
        );
        
        return resultado;
    }

    /**
     * Obtém o ranking dos top 5 clientes (usando pageable padrão)
     */
    public List<TopClienteDTO> obterTop5ClientesPorCompras() {
        return obterTop5ClientesPorCompras(PageRequest.of(0, 5));
    }

    /**
     * Calcula o ticket médio de compras por cliente
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "ticketMedio")
    public List<TicketMedioDTO> obterTicketMedioPorCliente() {
        log.debug("Gerando relatório: Ticket médio por cliente");
        
        List<TicketMedioDTO> resultado = pedidoRepository.findTicketMedioPorCliente();
        
        log.info("Relatório gerado: ticket médio calculado para {} clientes", resultado.size());
        
        // Log dos tickets mais altos para análise
        resultado.stream()
                .filter(ticket -> ticket.ticketMedio().compareTo(new BigDecimal("1000")) > 0)
                .forEach(ticket -> 
                    log.debug("Cliente VIP: {} - Ticket médio: R$ {}", 
                            ticket.nomeCliente(), ticket.ticketMedio())
                );
        
        return resultado;
    }

    /**
     * Calcula o faturamento total de um mês específico
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "faturamentoMensal", key = "#ano + '_' + #mes")
    public BigDecimal calcularFaturamentoMensal(int ano, int mes) {
        log.debug("Calculando faturamento mensal: {}/{}", mes, ano);
        
        // Validar parâmetros usando o validator
        pedidoValidator.validarParametrosRelatorio(ano, mes);
        
        BigDecimal faturamento = pedidoRepository.calcularFaturamentoMensal(ano, mes);
        BigDecimal resultado = faturamento != null ? faturamento : BigDecimal.ZERO;
        
        log.info("Faturamento de {}/{}: R$ {}", mes, ano, resultado);
        
        return resultado;
    }

    /**
     * Calcula o faturamento do mês atual
     */
    public BigDecimal calcularFaturamentoMesAtual() {
        LocalDateTime agora = LocalDateTime.now();
        return calcularFaturamentoMensal(agora.getYear(), agora.getMonthValue());
    }

    /**
     * Gera relatório consolidado de performance de vendas
     */
    public RelatorioPerformanceDTO gerarRelatorioPerformance(int ano, int mes) {
        log.info("Gerando relatório consolidado de performance para {}/{}", mes, ano);
        
        BigDecimal faturamento = calcularFaturamentoMensal(ano, mes);
        List<TopClienteDTO> topClientes = obterTop5ClientesPorCompras();
        List<TicketMedioDTO> ticketMedio = obterTicketMedioPorCliente();
        
        // Calcular estatísticas adicionais
        BigDecimal ticketMedioGeral = BigDecimal.ZERO;
        if (!ticketMedio.isEmpty()) {
            ticketMedioGeral = ticketMedio.stream()
                    .map(TicketMedioDTO::ticketMedio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(ticketMedio.size()), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        RelatorioPerformanceDTO relatorio = new RelatorioPerformanceDTO(
                faturamento,
                topClientes,
                ticketMedio,
                ticketMedioGeral,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        );
        
        log.info("Relatório de performance gerado com sucesso");
        return relatorio;
    }



    /**
     * DTO para relatório consolidado de performance
     */
    public record RelatorioPerformanceDTO(
            BigDecimal faturamentoMensal,
            List<TopClienteDTO> topClientes,
            List<TicketMedioDTO> ticketMedioClientes,
            BigDecimal ticketMedioGeral,
            String dataGeracao
    ) {}
}
