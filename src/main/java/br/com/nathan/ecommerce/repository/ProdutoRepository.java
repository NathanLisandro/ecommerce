package br.com.nathan.ecommerce.repository;

import br.com.nathan.ecommerce.domain.Produtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produtos, UUID> {
    
    Page<Produtos> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
    
    Page<Produtos> findByCategoriaIgnoreCase(String categoria, Pageable pageable);
    
    @Query("SELECT p FROM Produtos p WHERE p.quantidadeEstoque > 0")
    Page<Produtos> findProdutosEmEstoque(Pageable pageable);
    
    @Query("SELECT DISTINCT p.categoria FROM Produtos p WHERE p.categoria IS NOT NULL")
    List<String> findAllCategorias();
    
    @Query("SELECT p FROM Produtos p WHERE p.quantidadeEstoque >= :quantidade")
    List<Produtos> findProdutosComEstoque(@Param("quantidade") Long quantidade);
}
