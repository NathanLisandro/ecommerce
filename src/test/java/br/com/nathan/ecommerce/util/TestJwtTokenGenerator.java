package br.com.nathan.ecommerce.util;

import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.repository.UserRepository;
import br.com.nathan.ecommerce.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestJwtTokenGenerator {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    public String generateTokenForUser(String login) {
        User user = (User) userRepository.findByLogin(login);
        if (user == null) {
            throw new RuntimeException("Usuário não encontrado: " + login);
        }
        return tokenService.generateToken(user);
    }

    public String generateAdminToken() {
        return generateTokenForUser("admin");
    }

    public String generateUserToken() {
        return generateTokenForUser("user");
    }
}
