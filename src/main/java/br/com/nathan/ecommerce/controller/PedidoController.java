package br.com.nathan.ecommerce.controller;

import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoResponseDTO;
import br.com.nathan.ecommerce.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponseDTO> criarPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        PedidoResponseDTO pedido = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponseDTO> buscarPedido(@PathVariable UUID id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PedidoResponseDTO>> listarTodosPedidos(
            @PageableDefault(size = 10, sort = "dataCadastro") Pageable pageable) {
        Page<PedidoResponseDTO> pedidos = pedidoService.listarTodosPedidos(pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PedidoResponseDTO>> listarPedidosDoCliente(
            @PathVariable UUID clienteId,
            @PageableDefault(size = 10, sort = "dataCadastro") Pageable pageable) {
        Page<PedidoResponseDTO> pedidos = pedidoService.listarPedidosDoCliente(clienteId, pageable);
        return ResponseEntity.ok(pedidos);
    }

    @PostMapping("/{id}/pagamento")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponseDTO> processarPagamento(@PathVariable UUID id) {
        PedidoResponseDTO pedido = pedidoService.processarPagamento(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable UUID id) {
        PedidoResponseDTO pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}
