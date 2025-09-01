package br.com.nathan.ecommerce.controller;

import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.dto.ProdutoResponseDTO;
import br.com.nathan.ecommerce.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> criarProduto(@Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO produto = produtoService.criarProduto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarProduto(@PathVariable UUID id) {
        ProdutoResponseDTO produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(produto);
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> listarProdutos(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false, defaultValue = "false") boolean apenasEmEstoque) {
        
        Page<ProdutoResponseDTO> produtos;
        
        if (nome != null && !nome.trim().isEmpty()) {
            produtos = produtoService.buscarPorNome(nome, pageable);
        } else if (categoria != null && !categoria.trim().isEmpty()) {
            produtos = produtoService.buscarPorCategoria(categoria, pageable);
        } else if (apenasEmEstoque) {
            produtos = produtoService.listarProdutosEmEstoque(pageable);
        } else {
            produtos = produtoService.listarTodos(pageable);
        }
        
        return ResponseEntity.ok(produtos);
    }

    @GetMapping("/categorias")
    public ResponseEntity<List<String>> listarCategorias() {
        List<String> categorias = produtoService.listarCategorias();
        return ResponseEntity.ok(categorias);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponseDTO> atualizarProduto(
            @PathVariable UUID id,
            @Valid @RequestBody ProdutoRequestDTO dto) {
        ProdutoResponseDTO produto = produtoService.atualizarProduto(id, dto);
        return ResponseEntity.ok(produto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarProduto(@PathVariable UUID id) {
        produtoService.deletarProduto(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estoque")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> atualizarEstoque(
            @PathVariable UUID id,
            @RequestParam Long quantidade) {
        produtoService.atualizarEstoque(id, quantidade);
        return ResponseEntity.ok().build();
    }
}
