package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.dto.AuthenticationDTO;
import br.com.nathan.ecommerce.dto.LoginResponseDTO;
import br.com.nathan.ecommerce.dto.RegisterDTO;
import br.com.nathan.ecommerce.fixtures.UserFixture;
import br.com.nathan.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest extends BaseUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private AuthenticationDTO authenticationDTO;
    private RegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        testUser = UserFixture.createValidUser();
        authenticationDTO = UserFixture.createValidAuthenticationDTO();
        registerDTO = UserFixture.createValidRegisterDTO();
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void deveFazerLoginComSucesso() {
        String expectedToken = "jwt-token-example";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(tokenService.generateToken(testUser)).thenReturn(expectedToken);

        LoginResponseDTO result = authenticationService.login(authenticationDTO);

        assertNotNull(result);
        assertEquals(expectedToken, result.token());
        
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void deveRegistrarNovoUsuarioComSucesso() {
        User newUser = UserFixture.createUserWithLogin("newuser");

        when(userRepository.findByLogin("newuser")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = authenticationService.register(registerDTO);

        assertNotNull(result);
        assertEquals("newuser", result.getLogin());
        
        verify(userRepository, times(1)).findByLogin("newuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar usuário já existente")
    void deveLancarExcecaoAoRegistrarUsuarioJaExistente() {
        when(userRepository.findByLogin("newuser")).thenReturn(testUser);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.register(registerDTO));
        
        assertEquals("Usuário já existe!", exception.getMessage());
        verify(userRepository, times(1)).findByLogin("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve registrar usuário ADMIN com sucesso")
    void deveRegistrarUsuarioAdminComSucesso() {
        RegisterDTO adminRegisterDTO = UserFixture.createAdminRegisterDTO();
        User adminUser = UserFixture.createValidAdmin();

        when(userRepository.findByLogin("admin")).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(adminUser);

        User result = authenticationService.register(adminRegisterDTO);

        assertNotNull(result);
        assertEquals("admin", result.getLogin());
        
        verify(userRepository, times(1)).findByLogin("admin");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar token de autenticação correto")
    void deveCriarTokenDeAutenticacaoCorreto() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(tokenService.generateToken(testUser)).thenReturn("valid-jwt-token");

        LoginResponseDTO result = authenticationService.login(authenticationDTO);

        assertNotNull(result);
        assertEquals("valid-jwt-token", result.token());
        
        verify(tokenService, times(1)).generateToken(testUser);
    }

    @Test
    @DisplayName("Deve verificar se senha é codificada ao registrar")
    void deveVerificarSeSenhaECodificadaAoRegistrar() {
        String rawPassword = "password123";
        String encodedPassword = "encoded-password-hash";
        
        when(userRepository.findByLogin("newuser")).thenReturn(null);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        authenticationService.register(registerDTO);

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve propagar exceção de autenticação")
    void devePropagExcecaoDeAutenticacao() {
        RuntimeException authException = new RuntimeException("Credenciais inválidas");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(authException);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authenticationService.login(authenticationDTO));
        
        assertEquals("Credenciais inválidas", exception.getMessage());
        verify(tokenService, never()).generateToken(any(User.class));
    }
}