package br.com.nathan.ecommerce.fixtures;

import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.dto.AuthenticationDTO;
import br.com.nathan.ecommerce.dto.RegisterDTO;
import br.com.nathan.ecommerce.enums.UserRole;

import java.util.UUID;

public class UserFixture {

    public static User createValidUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .login("testuser")
                .password("encodedPassword123")
                .role(UserRole.USER)
                .build();
    }

    public static User createValidAdmin() {
        return User.builder()
                .id(UUID.randomUUID())
                .login("admin")
                .password("encodedPassword123")
                .role(UserRole.ADMIN)
                .build();
    }

    public static User createUserWithLogin(String login) {
        return User.builder()
                .id(UUID.randomUUID())
                .login(login)
                .password("encodedPassword123")
                .role(UserRole.USER)
                .build();
    }

    public static AuthenticationDTO createValidAuthenticationDTO() {
        return new AuthenticationDTO("testuser", "password123");
    }

    public static AuthenticationDTO createAuthenticationDTOWithCredentials(String login, String password) {
        return new AuthenticationDTO(login, password);
    }

    public static RegisterDTO createValidRegisterDTO() {
        return new RegisterDTO("newuser", "password123", UserRole.USER);
    }

    public static RegisterDTO createAdminRegisterDTO() {
        return new RegisterDTO("admin", "password123", UserRole.ADMIN);
    }

    public static RegisterDTO createRegisterDTOWithLogin(String login) {
        return new RegisterDTO(login, "password123", UserRole.USER);
    }
}
