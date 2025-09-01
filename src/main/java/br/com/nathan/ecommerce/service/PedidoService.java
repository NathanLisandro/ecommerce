package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.Cliente;
import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoResponseDTO;

import br.com.nathan.ecommerce.enums.PedidoStatus;
import br.com.nathan.ecommerce.mapper.PedidoMapper;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.exception.EstoqueInsuficienteException;
import br.com.nathan.ecommerce.repository.ClienteRepository;
import br.com.nathan.ecommerce.repository.PedidoRepository;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import br.com.nathan.ecommerce.validator.PedidoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueService estoqueService;
    private final PagamentoService pagamentoService;
    private final PedidoValidator pedidoValidator;

    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        // Buscar cliente
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente", dto.clienteId()));

        // Validar dados do pedido
        pedidoValidator.validarCriacaoPedido(dto, cliente);

        // Verificar estoque de todos os produtos antes de criar o pedido
        estoqueService.validarEstoqueDisponivel(dto.itens());

        // Criar o pedido
        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .status(PedidoStatus.PENDENTE)
                .valorTotal(BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();

        // Criar itens do pedido usando streams
        List<ItemPedido> itens = dto.itens().stream()
                .map(itemDto -> {
                    Produtos produto = produtoRepository.findById(itemDto.produtoId()).get();
                    return ItemPedido.builder()
                            .produtos(produto)
                            .quantidade(itemDto.quantidade())
                            .precoUnitario(produto.getPreco())
                            .build();
                })
                .toList();
        
        // Definir o pedido para cada item e calcular valor total
        Pedido finalPedido = pedido;
        BigDecimal valorTotal = itens.stream()
                .peek(item -> item.setPedido(finalPedido))
                .map(ItemPedido::getPrecoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        pedido.getItens().addAll(itens);
        pedido.setValorTotal(valorTotal);
        pedido = pedidoRepository.save(pedido);
        
        return PedidoMapper.toResponseDTO(pedido);
    }

    @Transactional(readOnly = true)
    public PedidoResponseDTO buscarPorId(UUID id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido", id));
        return PedidoMapper.toResponseDTO(pedido);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarPedidosDoCliente(UUID clienteId, Pageable pageable) {
        return pedidoRepository.findByClienteId(clienteId, pageable)
                .map(PedidoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarTodosPedidos(Pageable pageable) {
        return pedidoRepository.findAll(pageable)
                .map(PedidoMapper::toResponseDTO);
    }

    @Transactional
    public PedidoResponseDTO processarPagamento(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido", pedidoId));

        // Validar se o pedido pode ser processado
        pedidoValidator.validarProcessamentoPagamento(pedido);

        // Verificar estoque antes do pagamento (pode ter mudado desde a criação do pedido)
        try {
            estoqueService.verificarEstoqueParaPagamento(pedido.getItens());
        } catch (EstoqueInsuficienteException e) {
            // Se não há estoque, cancelar o pedido
            pedido.setStatus(PedidoStatus.CANCELADO);
            pedidoRepository.save(pedido);
            throw e;
        }

        // Processar pagamento
        boolean pagamentoAprovado = pagamentoService.processarPagamento(pedido);
        
        if (pagamentoAprovado) {
            // Atualizar estoque após pagamento aprovado
            estoqueService.atualizarEstoqueAposVenda(pedido.getItens());
            pedido.setStatus(PedidoStatus.APROVADO);
        } else {
            pedido.setStatus(PedidoStatus.REPROVADO);
        }

        pedido = pedidoRepository.save(pedido);
        return PedidoMapper.toResponseDTO(pedido);
    }

    @Transactional
    public PedidoResponseDTO cancelarPedido(UUID pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido", pedidoId));

        // Validar se o pedido pode ser cancelado
        pedidoValidator.validarCancelamentoPedido(pedido);

        pedido.setStatus(PedidoStatus.CANCELADO);
        pedido = pedidoRepository.save(pedido);
        return PedidoMapper.toResponseDTO(pedido);
    }


}
