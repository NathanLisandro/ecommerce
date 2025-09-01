package br.com.nathan.ecommerce.controller;

import br.com.nathan.ecommerce.dto.TicketMedioDTO;
import br.com.nathan.ecommerce.dto.TopClienteDTO;
import br.com.nathan.ecommerce.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/top-clientes")
    public ResponseEntity<List<TopClienteDTO>> obterTop5Clientes() {
        List<TopClienteDTO> topClientes = relatorioService.obterTop5ClientesPorCompras(PageRequest.of(0, 5));
        return ResponseEntity.ok(topClientes);
    }

    @GetMapping("/ticket-medio")
    public ResponseEntity<List<TicketMedioDTO>> obterTicketMedioPorCliente() {
        List<TicketMedioDTO> ticketMedio = relatorioService.obterTicketMedioPorCliente();
        return ResponseEntity.ok(ticketMedio);
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<BigDecimal> obterFaturamentoMensal(
            @RequestParam int ano,
            @RequestParam int mes) {
        
        BigDecimal faturamento = relatorioService.calcularFaturamentoMensal(ano, mes);
        return ResponseEntity.ok(faturamento);
    }
}
