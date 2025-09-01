package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.dto.ProdutoResponseDTO;
import br.com.nathan.ecommerce.exception.EntityNotFoundException;
import br.com.nathan.ecommerce.mapper.ProdutoMapper;
import br.com.nathan.ecommerce.repository.ProdutoRepository;
import br.com.nathan.ecommerce.validator.ProdutoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoValidator produtoValidator;

    @Transactional
    public ProdutoResponseDTO criarProduto(ProdutoRequestDTO dto) {
        // Validar dados do produto
        produtoValidator.validarDadosProduto(dto);
        
        Produtos produto = ProdutoMapper.toEntity(dto);
        produto = produtoRepository.save(produto);
        return ProdutoMapper.toResponseDTO(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(UUID id) {
        Produtos produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto", id));
        return ProdutoMapper.toResponseDTO(produto);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarTodos(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(ProdutoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        // Validar parâmetros de busca
        produtoValidator.validarParametrosBusca(nome, null);
        
        return produtoRepository.findByNomeContainingIgnoreCase(nome, pageable)
                .map(ProdutoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> buscarPorCategoria(String categoria, Pageable pageable) {
        // Validar parâmetros de busca
        produtoValidator.validarParametrosBusca(null, categoria);
        
        return produtoRepository.findByCategoriaIgnoreCase(categoria, pageable)
                .map(ProdutoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProdutoResponseDTO> listarProdutosEmEstoque(Pageable pageable) {
        return produtoRepository.findProdutosEmEstoque(pageable)
                .map(ProdutoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<String> listarCategorias() {
        return produtoRepository.findAllCategorias();
    }

    @Transactional
    public ProdutoResponseDTO atualizarProduto(UUID id, ProdutoRequestDTO dto) {
        // Validar dados do produto
        produtoValidator.validarDadosProduto(dto);
        
        Produtos produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto", id));
        
        ProdutoMapper.updateEntity(produto, dto);
        produto = produtoRepository.save(produto);
        return ProdutoMapper.toResponseDTO(produto);
    }

    @Transactional
    public void deletarProduto(UUID id) {
        if (!produtoRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto", id);
        }
        produtoRepository.deleteById(id);
    }

    @Transactional
    public void atualizarEstoque(UUID produtoId, Long novaQuantidade) {
        // Validar quantidade de estoque
        produtoValidator.validarAtualizacaoEstoque(novaQuantidade);
        
        Produtos produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto", produtoId));
        
        produto.setQuantidadeEstoque(novaQuantidade);
        produtoRepository.save(produto);
    }

    @Transactional
    public boolean verificarEstoque(UUID produtoId, Integer quantidade) {
        Produtos produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new EntityNotFoundException("Produto", produtoId));
        
        return produto.getQuantidadeEstoque() >= quantidade;
    }
}
