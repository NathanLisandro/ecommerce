package br.com.nathan.ecommerce.repository;

import br.com.nathan.ecommerce.domain.Pedido;
import br.com.nathan.ecommerce.dto.TicketMedioDTO;
import br.com.nathan.ecommerce.dto.TopClienteDTO;
import br.com.nathan.ecommerce.enums.PedidoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    
    Page<Pedido> findByClienteId(UUID clienteId, Pageable pageable);
    
    List<Pedido> findByStatus(PedidoStatus status);
    
    @Query("SELECT p FROM Pedido p WHERE p.dataCadastro BETWEEN :inicio AND :fim")
    List<Pedido> findByDataCadastroBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.status = 'APROVADO' AND YEAR(p.dataCadastro) = :ano AND MONTH(p.dataCadastro) = :mes")
    BigDecimal calcularFaturamentoMensal(@Param("ano") int ano, @Param("mes") int mes);
    
    @Query("""
        SELECT new br.com.nathan.ecommerce.dto.TopClienteDTO(
            c.id, 
            c.nome, 
            COUNT(p.id), 
            SUM(p.valorTotal)
        )
        FROM Pedido p 
        JOIN p.cliente c 
        WHERE p.status = 'APROVADO' 
        GROUP BY c.id, c.nome 
        ORDER BY SUM(p.valorTotal) DESC
        """)
    List<TopClienteDTO> findTop5ClientesPorValorComprado(Pageable pageable);
    
    @Query("""
        SELECT new br.com.nathan.ecommerce.dto.TicketMedioDTO(
            c.id, 
            c.nome, 
            CAST(AVG(p.valorTotal) AS java.math.BigDecimal)
        )
        FROM Pedido p 
        JOIN p.cliente c 
        WHERE p.status = 'APROVADO' 
        GROUP BY c.id, c.nome 
        ORDER BY c.nome
        """)
    List<TicketMedioDTO> findTicketMedioPorCliente();
}
