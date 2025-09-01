package br.com.nathan.ecommerce.validator;

import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class ProdutoValidator {

    /**
     * Valida dados de criação/atualização de produto
     */
    public void validarDadosProduto(ProdutoRequestDTO dto) {
        log.debug("Validando dados do produto: {}", dto.nome());
        
        validarNome(dto.nome());
        validarDescricao(dto.descricao());
        validarPreco(dto.preco());
        validarCategoria(dto.categoria());
        validarQuantidadeEstoque(dto.quantidadeEstoque());
        
        log.debug("Validação de produto aprovada para: {}", dto.nome());
    }

    /**
     * Valida parâmetros de busca e filtros
     */
    public void validarParametrosBusca(String nome, String categoria) {
        if (nome != null && !nome.trim().isEmpty()) {
            validarTermoBusca(nome, "Nome");
        }
        
        if (categoria != null && !categoria.trim().isEmpty()) {
            validarTermoBusca(categoria, "Categoria");
        }
    }

    /**
     * Valida atualização de estoque
     */
    public void validarAtualizacaoEstoque(Long quantidade) {
        log.debug("Validando atualização de estoque: {}", quantidade);
        
        if (quantidade == null) {
            throw new IllegalArgumentException("Quantidade de estoque é obrigatória");
        }
        
        if (quantidade < 0) {
            throw new IllegalArgumentException("Quantidade de estoque não pode ser negativa");
        }
        
        if (quantidade > 1_000_000L) {
            throw new IllegalArgumentException("Quantidade de estoque não pode exceder 1.000.000 unidades");
        }
        
        log.debug("Validação de atualização de estoque aprovada");
    }

    /**
     * Valida nome do produto
     */
    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do produto é obrigatório");
        }
        
        if (nome.length() < 2) {
            throw new IllegalArgumentException("Nome do produto deve ter pelo menos 2 caracteres");
        }
        
        if (nome.length() > 255) {
            throw new IllegalArgumentException("Nome do produto não pode exceder 255 caracteres");
        }
        
        // Validar caracteres especiais perigosos
        if (nome.matches(".*[<>\"'&].*")) {
            throw new IllegalArgumentException("Nome do produto contém caracteres não permitidos");
        }
    }

    /**
     * Valida descrição do produto
     */
    private void validarDescricao(String descricao) {
        if (descricao != null && descricao.length() > 1000) {
            throw new IllegalArgumentException("Descrição do produto não pode exceder 1000 caracteres");
        }
    }

    /**
     * Valida preço do produto
     */
    private void validarPreco(BigDecimal preco) {
        if (preco == null) {
            throw new IllegalArgumentException("Preço do produto é obrigatório");
        }
        
        if (preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço do produto deve ser maior que zero");
        }
        
        // Preço máximo: R$ 1.000.000
        BigDecimal precoMaximo = new BigDecimal("1000000.00");
        if (preco.compareTo(precoMaximo) > 0) {
            throw new IllegalArgumentException("Preço do produto não pode exceder R$ 1.000.000,00");
        }
        
        // Validar número de casas decimais
        if (preco.scale() > 2) {
            throw new IllegalArgumentException("Preço do produto deve ter no máximo 2 casas decimais");
        }
    }

    /**
     * Valida categoria do produto
     */
    private void validarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria do produto é obrigatória");
        }
        
        if (categoria.length() < 2) {
            throw new IllegalArgumentException("Categoria deve ter pelo menos 2 caracteres");
        }
        
        if (categoria.length() > 100) {
            throw new IllegalArgumentException("Categoria não pode exceder 100 caracteres");
        }
        
        // Validar formato da categoria (apenas letras, números, espaços e hífens)
        if (!categoria.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-]+$")) {
            throw new IllegalArgumentException("Categoria contém caracteres não permitidos");
        }
    }

    /**
     * Valida quantidade em estoque
     */
    private void validarQuantidadeEstoque(Long quantidadeEstoque) {
        if (quantidadeEstoque == null) {
            throw new IllegalArgumentException("Quantidade em estoque é obrigatória");
        }
        
        if (quantidadeEstoque < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        }
        
        if (quantidadeEstoque > 1_000_000L) {
            throw new IllegalArgumentException("Quantidade em estoque não pode exceder 1.000.000 unidades");
        }
    }

    /**
     * Valida termo de busca genérico
     */
    private void validarTermoBusca(String termo, String campo) {
        if (termo.length() < 2) {
            throw new IllegalArgumentException(campo + " para busca deve ter pelo menos 2 caracteres");
        }
        
        if (termo.length() > 100) {
            throw new IllegalArgumentException(campo + " para busca não pode exceder 100 caracteres");
        }
        
        // Prevenir SQL injection básico
        if (termo.matches(".*[';\"\\\\].*")) {
            throw new IllegalArgumentException(campo + " contém caracteres não permitidos para busca");
        }
    }
}
