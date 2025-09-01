package br.com.nathan.ecommerce.repository;

import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.dto.TicketMedioDTO;
import br.com.nathan.ecommerce.dto.TopClienteDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    @DisplayName("Deve buscar pedidos por cliente ID")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveBuscarPedidosPorClienteId() {
        UUID clienteId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Pedido> result = pedidoRepository.findByClienteId(clienteId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(clienteId, result.getContent().get(0).getCliente().getId());
        assertEquals(clienteId, result.getContent().get(1).getCliente().getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando cliente não tem pedidos")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveRetornarListaVaziaQuandoClienteNaoTemPedidos() {
        UUID clienteId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Pedido> result = pedidoRepository.findByClienteId(clienteId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve buscar top clientes por valor comprado")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveBuscarTopClientesPorValorComprado() {
        Pageable pageable = PageRequest.of(0, 5);
        List<TopClienteDTO> result = pedidoRepository.findTop5ClientesPorValorComprado(pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        TopClienteDTO primeiro = result.get(0);
        assertNotNull(primeiro.clienteId()); // clienteId
        assertNotNull(primeiro.nomeCliente()); // nome
        assertNotNull(primeiro.totalPedidos()); // totalPedidos
        assertNotNull(primeiro.valorTotalComprado()); // valorTotal
        
        // João Silva deve ser o primeiro com maior valor total
        assertEquals("João Silva", primeiro.nomeCliente());
        assertEquals(new BigDecimal("2300.00"), primeiro.valorTotalComprado());
    }

    @Test
    @DisplayName("Deve calcular ticket médio por cliente")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveCalcularTicketMedioPorCliente() {
        List<TicketMedioDTO> result = pedidoRepository.findTicketMedioPorCliente();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // Verificar se João Silva está na lista
        boolean joaoEncontrado = false;
        for (TicketMedioDTO dto : result) {
            if ("João Silva".equals(dto.nomeCliente())) {
                joaoEncontrado = true;
                assertEquals(0, new BigDecimal("1150.00").compareTo(dto.ticketMedio())); // (1500 + 800) / 2
                break;
            }
        }
        assertTrue(joaoEncontrado, "João Silva deveria estar na lista de ticket médio");
    }

    @Test
    @DisplayName("Deve calcular faturamento mensal")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveCalcularFaturamentoMensal() {
        BigDecimal result = pedidoRepository.calcularFaturamentoMensal(2024, 8);

        assertNotNull(result);
        // Soma de todos os pedidos APROVADOS: 1500 + 800 + 1000 + 500 = 3800
        assertEquals(new BigDecimal("3800.00"), result);
    }

    @Test
    @DisplayName("Deve retornar null para faturamento sem vendas")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveRetornarNullParaFaturamentoSemVendas() {
        BigDecimal result = pedidoRepository.calcularFaturamentoMensal(2020, 1);
        
        assertTrue(result == null || result.equals(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Deve buscar pedidos por status")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveBuscarPedidosPorStatus() {
        List<Pedido> pendentes = pedidoRepository.findByStatus(PedidoStatus.PENDENTE);
        List<Pedido> aprovados = pedidoRepository.findByStatus(PedidoStatus.APROVADO);

        assertEquals(1, pendentes.size());
        assertEquals(4, aprovados.size());
        
        assertEquals(PedidoStatus.PENDENTE, pendentes.get(0).getStatus());
        aprovados.forEach(pedido -> assertEquals(PedidoStatus.APROVADO, pedido.getStatus()));
    }

    @Test
    @DisplayName("Deve buscar pedidos por período")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveBuscarPedidosPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.of(2024, 8, 1, 9, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2024, 8, 1, 12, 30, 0);
        
        List<Pedido> result = pedidoRepository.findByDataCadastroBetween(inicio, fim);

        assertEquals(3, result.size());
        
        result.forEach(pedido -> {
            assertTrue(pedido.getDataCadastro().isAfter(inicio.minusSeconds(1)));
            assertTrue(pedido.getDataCadastro().isBefore(fim.plusSeconds(1)));
        });
    }

    @Test
    @DisplayName("Deve verificar se dados do dataset estão corretos")
    @DatabaseSetup("/datasets/pedidos-only.xml")
    void deveVerificarDadosDoDataset() {
        List<Pedido> todosPedidos = pedidoRepository.findAll();
        
        assertEquals(5, todosPedidos.size());
        
        todosPedidos.forEach(pedido -> {
            assertNotNull(pedido.getCliente());
            assertNotNull(pedido.getCliente().getNome());
            assertNotNull(pedido.getStatus());
            assertNotNull(pedido.getValorTotal());
            assertTrue(pedido.getValorTotal().compareTo(BigDecimal.ZERO) > 0);
        });
    }
}