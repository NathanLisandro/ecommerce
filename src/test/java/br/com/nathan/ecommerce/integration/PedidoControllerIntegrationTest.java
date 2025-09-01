package br.com.nathan.ecommerce.integration;

import br.com.nathan.ecommerce.dto.ItemPedidoRequestDTO;
import br.com.nathan.ecommerce.dto.PedidoRequestDTO;
import br.com.nathan.ecommerce.util.TestJwtTokenGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class PedidoControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestJwtTokenGenerator jwtTokenGenerator;

    @Test
    @DisplayName("Deve criar pedido com sucesso")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml"})
    void deveCriarPedidoComSucesso() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        UUID clienteId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        UUID produtoId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO(produtoId, 2);
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(clienteId, List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value(clienteId.toString()))
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.itens.length()").value(1))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.valorTotal").value(1799.98));
    }

    @Test
    @DisplayName("Deve rejeitar pedido sem autenticação")
    @DatabaseSetup({"/datasets/clientes.xml", "/datasets/produtos.xml"})
    void deveRejeitarPedidoSemAutenticacao() throws Exception {
        UUID clienteId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        UUID produtoId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO(produtoId, 1);
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(clienteId, List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve rejeitar pedido com cliente inexistente")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/produtos.xml"})
    void deveRejeitarPedidoComClienteInexistente() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        UUID clienteInexistente = UUID.fromString("660e8400-e29b-41d4-a716-446655440999");
        UUID produtoId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO(produtoId, 1);
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(clienteInexistente, List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cliente não encontrado com ID: " + clienteInexistente));
    }

    @Test
    @DisplayName("Deve rejeitar pedido com produto inexistente")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml"})
    void deveRejeitarPedidoComProdutoInexistente() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        UUID clienteId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        UUID produtoInexistente = UUID.fromString("770e8400-e29b-41d4-a716-446655440999");
        
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO(produtoInexistente, 1);
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(clienteId, List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Produto não encontrado com ID: " + produtoInexistente));
    }

    @Test
    @DisplayName("Deve rejeitar pedido com estoque insuficiente")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml"})
    void deveRejeitarPedidoComEstoqueInsuficiente() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        UUID clienteId = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
        UUID produtoId = UUID.fromString("770e8400-e29b-41d4-a716-446655440001");
        
        // Solicitando mais do que há em estoque (estoque = 50)
        ItemPedidoRequestDTO item = new ItemPedidoRequestDTO(produtoId, 100);
        PedidoRequestDTO pedidoRequest = new PedidoRequestDTO(clienteId, List.of(item));

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Estoque insuficiente para o produto 'Smartphone XYZ'. Disponível: 50, Solicitado: 100"));
    }

    @Test
    @DisplayName("Deve buscar pedido por ID")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveBuscarPedidoPorId() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        String pedidoId = "550e8400-e29b-41d4-a716-446655440010"; // Pedido APROVADO

        mockMvc.perform(get("/api/pedidos/{id}", pedidoId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.nomeCliente").value("João Silva"))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.valorTotal").value(1500.0))
                .andExpect(jsonPath("$.itens.length()").value(2));
    }

    @Test
    @DisplayName("Deve listar pedidos do cliente")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveListarPedidosDoCliente() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        String clienteId = "660e8400-e29b-41d4-a716-446655440001";

        mockMvc.perform(get("/api/pedidos/cliente/{clienteId}", clienteId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].clienteId").value(clienteId))
                .andExpect(jsonPath("$.content[0].nomeCliente").value("João Silva"));
    }

    @Test
    @DisplayName("Deve processar pagamento do pedido")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveProcessarPagamentoDoPedido() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        String pedidoId = "550e8400-e29b-41d4-a716-446655440014"; // Pedido PENDENTE

        mockMvc.perform(post("/api/pedidos/{id}/pagamento", pedidoId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                // Status pode ser APROVADO ou REPROVADO (90% de chance de aprovação)
                .andExpect(jsonPath("$.status").value(org.hamcrest.Matchers.anyOf(
                    org.hamcrest.Matchers.is("APROVADO"),
                    org.hamcrest.Matchers.is("REPROVADO")
                )));
    }

    @Test
    @DisplayName("Deve cancelar pedido")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveCancelarPedido() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        String pedidoId = "550e8400-e29b-41d4-a716-446655440014"; // Pedido PENDENTE

        mockMvc.perform(put("/api/pedidos/{id}/cancelar", pedidoId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoId))
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("Deve listar todos os pedidos como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveListarTodosOsPedidosComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("Deve rejeitar listagem de todos pedidos como USER")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarListagemDeTodosPedidosComoUser() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios ao criar pedido")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveValidarDadosObrigatoriosAoCriarPedido() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();
        // Pedido sem cliente e sem itens
        PedidoRequestDTO pedidoInvalido = new PedidoRequestDTO(null, List.of());

        mockMvc.perform(post("/api/pedidos")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}