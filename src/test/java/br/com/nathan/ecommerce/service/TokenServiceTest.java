package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.fixtures.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest extends BaseUnitTest {

    private TokenService tokenService;
    private User testUser;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        ReflectionTestUtils.setField(tokenService, "secret", "test-secret-key-for-jwt-token-generation");
        testUser = UserFixture.createValidUser();
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void deveGerarTokenJWTValido() {
        String token = tokenService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    @DisplayName("Deve validar token JWT e retornar subject")
    void deveValidarTokenJWTERetornarSubject() {
        String token = tokenService.generateToken(testUser);

        String subject = tokenService.validateToken(token);

        assertEquals(testUser.getLogin(), subject);
    }

    @Test
    @DisplayName("Deve lançar exceção para token inválido")
    void deveLancarExcecaoParaTokenInvalido() {
        String tokenInvalido = "token.invalido.aqui";

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tokenService.validateToken(tokenInvalido));
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para token nulo")
    void deveLancarExcecaoParaTokenNulo() {
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tokenService.validateToken(null));
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para token vazio")
    void deveLancarExcecaoParaTokenVazio() {
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> tokenService.validateToken(""));
        assertEquals("Token inválido", exception.getMessage());
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
    void deveGerarTokensDiferentesParaUsuariosDiferentes() {
        User outroUsuario = UserFixture.createValidAdmin();

        String token1 = tokenService.generateToken(testUser);
        String token2 = tokenService.generateToken(outroUsuario);

        assertNotEquals(token1, token2);
    }
}
