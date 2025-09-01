package br.com.nathan.ecommerce.integration;

import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.service.TokenService;
import br.com.nathan.ecommerce.util.TestJwtTokenGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.ExpectedDatabase;
import com.github.springtestdbunit.assertion.DatabaseAssertionMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ProdutoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private TestJwtTokenGenerator jwtTokenGenerator;

    @Test
    @DisplayName("Deve listar produtos sem autenticação")
    @DatabaseSetup("/datasets/produtos.xml")
    void deveListarProdutosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].nome").value("Camiseta Basic"));
    }

    @Test
    @DisplayName("Deve buscar produto por ID")
    @DatabaseSetup("/datasets/produtos.xml")
    void deveBuscarProdutoPorId() throws Exception {
        String produtoId = "770e8400-e29b-41d4-a716-446655440001";
        
        mockMvc.perform(get("/api/produtos/{id}", produtoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produtoId))
                .andExpect(jsonPath("$.nome").value("Smartphone XYZ"))
                .andExpect(jsonPath("$.preco").value(899.99))
                .andExpect(jsonPath("$.categoria").value("Eletrônicos"));
    }

    @Test
    @DisplayName("Deve retornar 404 para produto inexistente")
    void deveRetornar404ParaProdutoInexistente() throws Exception {
        String produtoIdInexistente = "550e8400-e29b-41d4-a716-446655440999";
        
        mockMvc.perform(get("/api/produtos/{id}", produtoIdInexistente))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Produto não encontrado com ID: " + produtoIdInexistente));
    }

    @Test
    @DisplayName("Deve criar produto como ADMIN")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveCriarProdutoComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();
        
        ProdutoRequestDTO produtoRequest = new ProdutoRequestDTO(
                "Tablet ABC",
                "Tablet para estudos",
                new BigDecimal("599.99"),
                "Eletrônicos",
                25L
        );

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Tablet ABC"))
                .andExpect(jsonPath("$.preco").value(599.99))
                .andExpect(jsonPath("$.categoria").value("Eletrônicos"))
                .andExpect(jsonPath("$.quantidadeEstoque").value(25));
    }

    @Test
    @DisplayName("Deve rejeitar criação de produto sem autenticação")
    void deveRejeitarCriacaoDeProdutoSemAutenticacao() throws Exception {
        ProdutoRequestDTO produtoRequest = new ProdutoRequestDTO(
                "Produto Teste",
                "Descrição teste",
                new BigDecimal("100.00"),
                "Teste",
                10L
        );

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve rejeitar criação de produto como USER")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarCriacaoDeProdutoComoUser() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        
        ProdutoRequestDTO produtoRequest = new ProdutoRequestDTO(
                "Produto Teste",
                "Descrição teste",
                new BigDecimal("100.00"),
                "Teste",
                10L
        );

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve atualizar produto como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/produtos.xml"})
    void deveAtualizarProdutoComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();
        String produtoId = "770e8400-e29b-41d4-a716-446655440001";
        
        ProdutoRequestDTO produtoRequest = new ProdutoRequestDTO(
                "Smartphone XYZ Atualizado",
                "Smartphone com 256GB de armazenamento",
                new BigDecimal("999.99"),
                "Eletrônicos",
                40L
        );

        mockMvc.perform(put("/api/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Smartphone XYZ Atualizado"))
                .andExpect(jsonPath("$.descricao").value("Smartphone com 256GB de armazenamento"))
                .andExpect(jsonPath("$.preco").value(999.99))
                .andExpect(jsonPath("$.quantidadeEstoque").value(40));
    }

    @Test
    @DisplayName("Deve deletar produto como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/produtos.xml"})
    void deveDeletarProdutoComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();
        String produtoId = "770e8400-e29b-41d4-a716-446655440003";

        mockMvc.perform(delete("/api/produtos/{id}", produtoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Verificar se o produto foi realmente deletado
        mockMvc.perform(get("/api/produtos/{id}", produtoId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve filtrar produtos por nome")
    @DatabaseSetup("/datasets/produtos.xml")
    void deveFiltrarProdutosPorNome() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .param("nome", "Smartphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Smartphone XYZ"));
    }

    @Test
    @DisplayName("Deve filtrar produtos por categoria")
    @DatabaseSetup("/datasets/produtos.xml")
    void deveFiltrarProdutosPorCategoria() throws Exception {
        mockMvc.perform(get("/api/produtos")
                        .param("categoria", "Eletrônicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Deve listar categorias disponíveis")
    @DatabaseSetup("/datasets/produtos.xml")
    void deveListarCategoriasDisponiveis() throws Exception {
        mockMvc.perform(get("/api/produtos/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("Eletrônicos"))
                .andExpect(jsonPath("$[1]").value("Roupas"));
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios ao criar produto")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveValidarDadosObrigatoriosAoCriarProduto() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();
        
        ProdutoRequestDTO produtoInvalido = new ProdutoRequestDTO(
                "", // nome vazio
                "Descrição",
                null, // preço nulo
                "Categoria",
                -1L // estoque negativo
        );

        mockMvc.perform(post("/api/produtos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(produtoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}
