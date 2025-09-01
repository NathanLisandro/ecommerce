package br.com.nathan.ecommerce.integration;

import br.com.nathan.ecommerce.dto.AuthenticationDTO;
import br.com.nathan.ecommerce.dto.RegisterDTO;
import br.com.nathan.ecommerce.enums.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class AuthenticationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve fazer login com sucesso")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveFazerLoginComSucesso() throws Exception {
        AuthenticationDTO loginRequest = new AuthenticationDTO("admin", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar login com credenciais inválidas")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarLoginComCredenciaisInvalidas() throws Exception {
        AuthenticationDTO loginRequest = new AuthenticationDTO("admin", "senha-errada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve rejeitar login com usuário inexistente")
    void deveRejeitarLoginComUsuarioInexistente() throws Exception {
        AuthenticationDTO loginRequest = new AuthenticationDTO("usuario-inexistente", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        RegisterDTO registerRequest = new RegisterDTO("novouser", "123456", UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.login").value("novouser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist()); // Senha não deve ser retornada
    }

    @Test
    @DisplayName("Deve rejeitar registro de usuário já existente")
    @DatabaseSetup("/datasets/usuarios.xml")
    void deveRejeitarRegistroDeUsuarioJaExistente() throws Exception {
        RegisterDTO registerRequest = new RegisterDTO("admin", "123456", UserRole.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Usuário já existe!"));
    }

    @Test
    @DisplayName("Deve registrar usuário ADMIN com sucesso")
    void deveRegistrarUsuarioAdminComSucesso() throws Exception {
        RegisterDTO registerRequest = new RegisterDTO("novoadmin", "123456", UserRole.ADMIN);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("novoadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios no registro")
    void deveValidarDadosObrigatoriosNoRegistro() throws Exception {
        // Teste com login vazio
        RegisterDTO registerInvalido = new RegisterDTO("", "123456", UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve validar dados obrigatórios no login")
    void deveValidarDadosObrigatoriosNoLogin() throws Exception {
        // Teste com login vazio
        AuthenticationDTO loginInvalido = new AuthenticationDTO("", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve aceitar requisições sem autenticação nos endpoints de auth")
    void deveAceitarRequisicoesEndpointsAuth() throws Exception {
        // Endpoints de autenticação devem ser públicos
        RegisterDTO registerRequest = new RegisterDTO("publico", "123456", UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Agora fazer login com o usuário criado
        AuthenticationDTO loginRequest = new AuthenticationDTO("publico", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Deve rejeitar requisições com JSON malformado")
    void deveRejeitarRequisicoesComJsonMalformado() throws Exception {
        String jsonMalformado = "{ login: admin, password: }";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMalformado))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve rejeitar requisições sem Content-Type")
    void deveRejeitarRequisicoesemContentType() throws Exception {
        AuthenticationDTO loginRequest = new AuthenticationDTO("admin", "123456");

        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
