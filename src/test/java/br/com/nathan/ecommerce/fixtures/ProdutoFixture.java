package br.com.nathan.ecommerce.fixtures;

import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProdutoFixture {

    public static Produtos createValidProduto() {
        return Produtos.builder()
                .nome("Smartphone XYZ")
                .descricao("Smartphone com 128GB")
                .preco(new BigDecimal("899.99"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(50L)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Produtos createProdutoWithId(UUID id) {
        return Produtos.builder()
                .id(id)
                .nome("Smartphone XYZ")
                .descricao("Smartphone com 128GB")
                .preco(new BigDecimal("899.99"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(50L)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Produtos createProdutoWithEstoque(Long estoque) {
        return Produtos.builder()
                .nome("Smartphone XYZ")
                .descricao("Smartphone com 128GB")
                .preco(new BigDecimal("899.99"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(estoque)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Produtos createNotebook() {
        return Produtos.builder()
                .nome("Notebook ABC")
                .descricao("Notebook para trabalho")
                .preco(new BigDecimal("2499.99"))
                .categoria("Eletrônicos")
                .quantidadeEstoque(20L)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Produtos createCamiseta() {
        return Produtos.builder()
                .nome("Camiseta Basic")
                .descricao("Camiseta 100% algodão")
                .preco(new BigDecimal("39.99"))
                .categoria("Roupas")
                .quantidadeEstoque(100L)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static ProdutoRequestDTO createValidProdutoRequestDTO() {
        return new ProdutoRequestDTO(
                "Smartphone XYZ",
                "Smartphone com 128GB",
                new BigDecimal("899.99"),
                "Eletrônicos",
                50L
        );
    }

    public static ProdutoRequestDTO createProdutoRequestDTOWithNome(String nome) {
        return new ProdutoRequestDTO(
                nome,
                "Descrição do produto",
                new BigDecimal("100.00"),
                "Categoria",
                10L
        );
    }

    public static ProdutoRequestDTO createInvalidProdutoRequestDTO() {
        return new ProdutoRequestDTO(
                "",
                "Descrição",
                null,
                "Categoria",
                -1L
        );
    }
}
