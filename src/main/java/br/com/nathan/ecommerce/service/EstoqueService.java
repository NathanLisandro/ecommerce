package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.ItemPedido;
import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.exception.EstoqueInsuficienteException;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstoqueService {

    private final ProdutoRepository produtoRepository;

    /**
     * Valida se há estoque suficiente para todos os itens solicitados
     */
    public void validarEstoqueDisponivel(List<ItemPedidoRequestDTO> itens) {
        log.debug("Validando estoque para {} itens", itens.size());
        
        itens.forEach(itemDto -> {
            Produtos produto = buscarProduto(itemDto.produtoId());
            
            if (produto.getQuantidadeEstoque() < itemDto.quantidade()) {
                log.warn("Estoque insuficiente para produto {}: disponível={}, solicitado={}", 
                        produto.getNome(), produto.getQuantidadeEstoque(), itemDto.quantidade());
                throw new EstoqueInsuficienteException(
                    itemDto.produtoId(), 
                    produto.getNome(), 
                    produto.getQuantidadeEstoque(), 
                    itemDto.quantidade()
                );
            }
        });
        
        log.debug("Validação de estoque concluída com sucesso");
    }

    /**
     * Verifica se há estoque suficiente para os itens do pedido antes do pagamento
     */
    public void verificarEstoqueParaPagamento(List<ItemPedido> itens) {
        log.debug("Verificando estoque para pagamento de {} itens", itens.size());
        
        itens.stream()
                .filter(item -> item.getProdutos().getQuantidadeEstoque() < item.getQuantidade())
                .findFirst()
                .ifPresent(item -> {
                    log.warn("Estoque insuficiente para pagamento do produto {}", 
                            item.getProdutos().getNome());
                    throw new EstoqueInsuficienteException(item.getProdutos().getNome());
                });
    }

    /**
     * Atualiza o estoque dos produtos após confirmação do pagamento
     */
    @Transactional
    public void atualizarEstoqueAposVenda(List<ItemPedido> itens) {
        log.debug("Atualizando estoque após venda para {} itens", itens.size());
        
        List<Produtos> produtosParaAtualizar = itens.stream()
                .map(item -> {
                    Produtos produto = item.getProdutos();
                    Long estoqueAnterior = produto.getQuantidadeEstoque();
                    Long novoEstoque = estoqueAnterior - item.getQuantidade();
                    
                    produto.setQuantidadeEstoque(novoEstoque);
                    
                    log.debug("Produto {}: estoque {} -> {}", 
                            produto.getNome(), estoqueAnterior, novoEstoque);
                    
                    return produto;
                })
                .toList();
        
        produtoRepository.saveAll(produtosParaAtualizar);
        log.info("Estoque atualizado para {} produtos", produtosParaAtualizar.size());
    }

    /**
     * Verifica disponibilidade de estoque para um produto específico
     */
    public boolean verificarDisponibilidade(UUID produtoId, Integer quantidade) {
        Produtos produto = buscarProduto(produtoId);
        return produto.getQuantidadeEstoque() >= quantidade;
    }

    private Produtos buscarProduto(UUID produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto", produtoId));
    }
}
