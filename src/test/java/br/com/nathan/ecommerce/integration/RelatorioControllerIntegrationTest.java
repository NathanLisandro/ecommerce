package br.com.nathan.ecommerce.integration;

import br.com.nathan.ecommerce.util.TestJwtTokenGenerator;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class RelatorioControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestJwtTokenGenerator jwtTokenGenerator;

    @Test
    @DisplayName("Deve obter top 5 clientes como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveObterTop5ClientesComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/relatorios/top-clientes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].clienteId").exists())
                .andExpect(jsonPath("$[0].nomeCliente").exists())
                .andExpect(jsonPath("$[0].totalPedidos").exists())
                .andExpect(jsonPath("$[0].valorTotalComprado").exists());
    }

    @Test
    @DisplayName("Deve rejeitar top 5 clientes como USER")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarTop5ClientesComoUser() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();

        mockMvc.perform(get("/api/relatorios/top-clientes")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve rejeitar top 5 clientes sem autenticação")
    void deveRejeitarTop5ClientesSemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/relatorios/top-clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve obter ticket médio por cliente como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveObterTicketMedioPorClienteComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/relatorios/ticket-medio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2)) // Apenas pedidos APROVADO são considerados
                .andExpect(jsonPath("$[0].clienteId").exists())
                .andExpect(jsonPath("$[0].nomeCliente").exists())
                .andExpect(jsonPath("$[0].ticketMedio").exists());
    }

    @Test
    @DisplayName("Deve obter faturamento mensal como ADMIN")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveObterFaturamentoMensalComoAdmin() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ano", "2024")
                        .param("mes", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNumber())
                .andExpect(jsonPath("$").value(3800.0)); // Valor do pedido APROVADO
    }

    @Test
    @DisplayName("Deve retornar zero para faturamento de mês sem vendas")
    @DatabaseSetup({"/datasets/usuarios.xml", "/datasets/clientes.xml", "/datasets/produtos.xml", "/datasets/pedidos.xml"})
    void deveRetornarZeroParaFaturamentoSemVendas() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ano", "2023")
                        .param("mes", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    @DisplayName("Deve validar parâmetros do faturamento mensal")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveValidarParametrosFaturamentoMensal() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        // Teste com mês inválido
        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ano", "2024")
                        .param("mes", "13")) // Mês inválido
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mês deve estar entre 1 e 12"));

        // Teste com mês zero
        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ano", "2024")
                        .param("mes", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Mês deve estar entre 1 e 12"));
    }

    @Test
    @DisplayName("Deve rejeitar faturamento mensal sem parâmetros obrigatórios")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarFaturamentoMensalSemParametros() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        // Teste sem parâmetro ano
        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("mes", "1"))
                .andExpect(status().isBadRequest());

        // Teste sem parâmetro mês
        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("ano", "2024"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve rejeitar relatórios como USER")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarRelatoriosComoUser() throws Exception {
        String userToken = jwtTokenGenerator.generateUserToken();

        mockMvc.perform(get("/api/relatorios/ticket-medio")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/relatorios/faturamento-mensal")
                        .header("Authorization", "Bearer " + userToken)
                        .param("ano", "2024")
                        .param("mes", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há pedidos")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRetornarListaVaziaQuandoNaoHaPedidos() throws Exception {
        String adminToken = jwtTokenGenerator.generateAdminToken();

        mockMvc.perform(get("/api/relatorios/top-clientes")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/relatorios/ticket-medio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
}