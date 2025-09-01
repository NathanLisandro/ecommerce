package br.com.nathan.ecommerce.mapper;

import br.com.nathan.ecommerce.domain.Produtos;
import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.dto.ProdutoResponseDTO;

public class ProdutoMapper {
    
    public static Produtos toEntity(ProdutoRequestDTO dto) {
        return Produtos.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .preco(dto.preco())
                .categoria(dto.categoria())
                .quantidadeEstoque(dto.quantidadeEstoque())
                .build();
    }
    
    public static ProdutoResponseDTO toResponseDTO(Produtos produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getCategoria(),
                produto.getQuantidadeEstoque(),
                produto.getDataCadastro(),
                produto.getDataAtualizacao()
        );
    }
    
    public static void updateEntity(Produtos produto, ProdutoRequestDTO dto) {
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setCategoria(dto.categoria());
        produto.setQuantidadeEstoque(dto.quantidadeEstoque());
    }
}
