package br.com.nathan.ecommerce.exception;

import java.util.UUID;

/**
 * Exceção lançada quando não há estoque suficiente para um produto
 */
public class EstoqueInsuficienteException extends BusinessException {
    
    private final UUID produtoId;
    private final String nomeProduto;
    private final Long estoqueDisponivel;
    private final Integer quantidadeSolicitada;

    public EstoqueInsuficienteException(UUID produtoId, String nomeProduto, 
                                      Long estoqueDisponivel, Integer quantidadeSolicitada) {
        super(String.format(
            "Estoque insuficiente para o produto '%s'. Disponível: %d, Solicitado: %d",
            nomeProduto, estoqueDisponivel, quantidadeSolicitada
        ));
        
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.estoqueDisponivel = estoqueDisponivel;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public EstoqueInsuficienteException(String nomeProduto) {
        super(String.format("Estoque insuficiente para o produto '%s'", nomeProduto));
        this.produtoId = null;
        this.nomeProduto = nomeProduto;
        this.estoqueDisponivel = null;
        this.quantidadeSolicitada = null;
    }

    public UUID getProdutoId() {
        return produtoId;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public Long getEstoqueDisponivel() {
        return estoqueDisponivel;
    }

    public Integer getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }
}
