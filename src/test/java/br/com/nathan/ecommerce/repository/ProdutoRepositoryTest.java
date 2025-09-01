package br.com.nathan.ecommerce.repository;

import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProdutoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProdutoRepository produtoRepository;

    private Produtos smartphone;
    private Produtos notebook;
    private Produtos camiseta;

    @BeforeEach
    void setUp() {
        smartphone = ProdutoFixture.createValidProduto();
        notebook = ProdutoFixture.createNotebook();
        camiseta = ProdutoFixture.createCamiseta();
        camiseta.setQuantidadeEstoque(0L);

        entityManager.persistAndFlush(smartphone);
        entityManager.persistAndFlush(notebook);
        entityManager.persistAndFlush(camiseta);
    }

    @Test
    @DisplayName("Deve buscar produtos por nome ignorando case")
    void deveBuscarProdutosPorNomeIgnorandoCase() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Produtos> resultado = produtoRepository.findByNomeContainingIgnoreCase("smartphone", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Smartphone XYZ", resultado.getContent().get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar produtos por nome parcial")
    void deveBuscarProdutosPorNomeParcial() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Produtos> resultado = produtoRepository.findByNomeContainingIgnoreCase("book", pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals("Notebook ABC", resultado.getContent().get(0).getNome());
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria ignorando case")
    void deveBuscarProdutosPorCategoriaIgnorandoCase() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Produtos> resultado = produtoRepository.findByCategoriaIgnoreCase("eletrônicos", pageable);

        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().stream()
                .allMatch(p -> p.getCategoria().equalsIgnoreCase("Eletrônicos")));
    }

    @Test
    @DisplayName("Deve buscar apenas produtos em estoque")
    void deveBuscarApenasProdutosEmEstoque() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Produtos> resultado = produtoRepository.findProdutosEmEstoque(pageable);

        assertEquals(2, resultado.getTotalElements());
        assertTrue(resultado.getContent().stream()
                .allMatch(p -> p.getQuantidadeEstoque() > 0));
    }

    @Test
    @DisplayName("Deve listar todas as categorias disponíveis")
    void deveListarTodasAsCategoriasDisponiveis() {
        List<String> categorias = produtoRepository.findAllCategorias();

        assertEquals(2, categorias.size());
        assertTrue(categorias.contains("Eletrônicos"));
        assertTrue(categorias.contains("Roupas"));
    }

    @Test
    @DisplayName("Deve buscar produtos com estoque mínimo")
    void deveBuscarProdutosComEstoqueMinimo() {
        List<Produtos> produtos = produtoRepository.findProdutosComEstoque(25L);

        assertEquals(1, produtos.size());
        assertEquals("Smartphone XYZ", produtos.get(0).getNome());
        assertTrue(produtos.get(0).getQuantidadeEstoque() >= 25L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar produtos por nome")
    void deveRetornarListaVaziaQuandoNaoEncontrarProdutosPorNome() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Produtos> resultado = produtoRepository.findByNomeContainingIgnoreCase("inexistente", pageable);

        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    @Test
    @DisplayName("Deve aplicar paginação corretamente")
    void deveAplicarPaginacaoCorretamente() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Produtos> resultado = produtoRepository.findAll(pageable);

        assertEquals(3, resultado.getTotalElements());
        assertEquals(2, resultado.getSize());
        assertEquals(2, resultado.getNumberOfElements());
        assertEquals(2, resultado.getTotalPages());
        assertEquals(0, resultado.getNumber());
    }

    @Test
    @DisplayName("Deve ordenar produtos corretamente")
    void deveOrdenarProdutosCorretamente() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("preco").ascending());

        Page<Produtos> resultado = produtoRepository.findAll(pageable);

        List<Produtos> produtos = resultado.getContent();
        assertEquals(3, produtos.size());
        
        assertTrue(produtos.get(0).getPreco().compareTo(produtos.get(1).getPreco()) <= 0);
        assertTrue(produtos.get(1).getPreco().compareTo(produtos.get(2).getPreco()) <= 0);
    }
}