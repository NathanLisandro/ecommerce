package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.dto.TicketMedioDTO;
import br.com.nathan.ecommerce.dto.TopClienteDTO;
import br.com.nathan.ecommerce.repository.PedidoRepository;
import br.com.nathan.ecommerce.validator.PedidoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RelatorioServiceTest extends BaseUnitTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoValidator pedidoValidator;

    @InjectMocks
    private RelatorioService relatorioService;

    private List<TopClienteDTO> topClientes;
    private List<TicketMedioDTO> ticketMedio;

    @BeforeEach
    void setUp() {
        topClientes = List.of(
                new TopClienteDTO(UUID.randomUUID(), "Cliente A", 5L, new BigDecimal("1000.00")),
                new TopClienteDTO(UUID.randomUUID(), "Cliente B", 3L, new BigDecimal("750.00"))
        );

        ticketMedio = List.of(
                new TicketMedioDTO(UUID.randomUUID(), "Cliente A", new BigDecimal("200.00")),
                new TicketMedioDTO(UUID.randomUUID(), "Cliente B", new BigDecimal("150.00"))
        );
    }

    @Test
    @DisplayName("Deve obter top 5 clientes por compras")
    void deveObterTop5ClientesPorCompras() {
        Pageable pageable = PageRequest.of(0, 5);
        when(pedidoRepository.findTop5ClientesPorValorComprado(pageable)).thenReturn(topClientes);

        List<TopClienteDTO> resultado = relatorioService.obterTop5ClientesPorCompras(pageable);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Cliente A", resultado.get(0).nomeCliente());
        assertEquals(new BigDecimal("1000.00"), resultado.get(0).valorTotalComprado());
        
        verify(pedidoRepository, times(1)).findTop5ClientesPorValorComprado(pageable);
    }

    @Test
    @DisplayName("Deve obter top 5 clientes com pageable padrão")
    void deveObterTop5ClientesComPageablePadrao() {
        when(pedidoRepository.findTop5ClientesPorValorComprado(any(Pageable.class))).thenReturn(topClientes);

        List<TopClienteDTO> resultado = relatorioService.obterTop5ClientesPorCompras();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        
        verify(pedidoRepository, times(1)).findTop5ClientesPorValorComprado(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve obter ticket médio por cliente")
    void deveObterTicketMedioPorCliente() {
        when(pedidoRepository.findTicketMedioPorCliente()).thenReturn(ticketMedio);

        List<TicketMedioDTO> resultado = relatorioService.obterTicketMedioPorCliente();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Cliente A", resultado.get(0).nomeCliente());
        assertEquals(new BigDecimal("200.00"), resultado.get(0).ticketMedio());
        
        verify(pedidoRepository, times(1)).findTicketMedioPorCliente();
    }

    @Test
    @DisplayName("Deve calcular faturamento mensal")
    void deveCalcularFaturamentoMensal() {
        int ano = 2024;
        int mes = 1;
        BigDecimal faturamento = new BigDecimal("5000.00");
        
        doNothing().when(pedidoValidator).validarParametrosRelatorio(ano, mes);
        when(pedidoRepository.calcularFaturamentoMensal(ano, mes)).thenReturn(faturamento);

        BigDecimal resultado = relatorioService.calcularFaturamentoMensal(ano, mes);

        assertEquals(faturamento, resultado);
        
        verify(pedidoValidator, times(1)).validarParametrosRelatorio(ano, mes);
        verify(pedidoRepository, times(1)).calcularFaturamentoMensal(ano, mes);
    }

    @Test
    @DisplayName("Deve retornar zero quando faturamento for nulo")
    void deveRetornarZeroQuandoFaturamentoForNulo() {
        int ano = 2024;
        int mes = 1;
        
        doNothing().when(pedidoValidator).validarParametrosRelatorio(ano, mes);
        when(pedidoRepository.calcularFaturamentoMensal(ano, mes)).thenReturn(null);

        BigDecimal resultado = relatorioService.calcularFaturamentoMensal(ano, mes);

        assertEquals(BigDecimal.ZERO, resultado);
        
        verify(pedidoValidator, times(1)).validarParametrosRelatorio(ano, mes);
        verify(pedidoRepository, times(1)).calcularFaturamentoMensal(ano, mes);
    }

    @Test
    @DisplayName("Deve calcular faturamento do mês atual")
    void deveCalcularFaturamentoDoMesAtual() {
        LocalDateTime agora = LocalDateTime.now();
        BigDecimal faturamento = new BigDecimal("3000.00");
        
        doNothing().when(pedidoValidator).validarParametrosRelatorio(agora.getYear(), agora.getMonthValue());
        when(pedidoRepository.calcularFaturamentoMensal(agora.getYear(), agora.getMonthValue())).thenReturn(faturamento);

        BigDecimal resultado = relatorioService.calcularFaturamentoMesAtual();

        assertEquals(faturamento, resultado);
    }

    @Test
    @DisplayName("Deve gerar relatório de performance consolidado")
    void deveGerarRelatorioDePerformanceConsolidado() {
        int ano = 2024;
        int mes = 1;
        BigDecimal faturamento = new BigDecimal("5000.00");
        
        doNothing().when(pedidoValidator).validarParametrosRelatorio(ano, mes);
        when(pedidoRepository.calcularFaturamentoMensal(ano, mes)).thenReturn(faturamento);
        when(pedidoRepository.findTop5ClientesPorValorComprado(any(Pageable.class))).thenReturn(topClientes);
        when(pedidoRepository.findTicketMedioPorCliente()).thenReturn(ticketMedio);

        RelatorioService.RelatorioPerformanceDTO resultado = relatorioService.gerarRelatorioPerformance(ano, mes);

        assertNotNull(resultado);
        assertEquals(faturamento, resultado.faturamentoMensal());
        assertEquals(2, resultado.topClientes().size());
        assertEquals(2, resultado.ticketMedioClientes().size());
        assertNotNull(resultado.ticketMedioGeral());
        assertNotNull(resultado.dataGeracao());
        
        // Verifica se o ticket médio geral foi calculado corretamente
        BigDecimal ticketEsperado = new BigDecimal("175.00"); // (200 + 150) / 2
        assertEquals(0, ticketEsperado.compareTo(resultado.ticketMedioGeral()));
    }

    @Test
    @DisplayName("Deve lidar com lista vazia de clientes")
    void deveLidarComListaVaziaDeClientes() {
        Pageable pageable = PageRequest.of(0, 5);
        when(pedidoRepository.findTop5ClientesPorValorComprado(pageable)).thenReturn(List.of());

        List<TopClienteDTO> resultado = relatorioService.obterTop5ClientesPorCompras(pageable);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve lidar com lista vazia de ticket médio")
    void deveLidarComListaVaziaDeTicketMedio() {
        when(pedidoRepository.findTicketMedioPorCliente()).thenReturn(List.of());

        List<TicketMedioDTO> resultado = relatorioService.obterTicketMedioPorCliente();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve gerar relatório com dados vazios")
    void deveGerarRelatorioComDadosVazios() {
        int ano = 2024;
        int mes = 1;
        
        doNothing().when(pedidoValidator).validarParametrosRelatorio(ano, mes);
        when(pedidoRepository.calcularFaturamentoMensal(ano, mes)).thenReturn(BigDecimal.ZERO);
        when(pedidoRepository.findTop5ClientesPorValorComprado(any(Pageable.class))).thenReturn(List.of());
        when(pedidoRepository.findTicketMedioPorCliente()).thenReturn(List.of());

        RelatorioService.RelatorioPerformanceDTO resultado = relatorioService.gerarRelatorioPerformance(ano, mes);

        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.faturamentoMensal());
        assertTrue(resultado.topClientes().isEmpty());
        assertTrue(resultado.ticketMedioClientes().isEmpty());
    }
}
