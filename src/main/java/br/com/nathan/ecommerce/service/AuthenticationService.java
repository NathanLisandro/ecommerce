package br.com.nathan.ecommerce.service;

import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.dto.AuthenticationDTO;
import br.com.nathan.ecommerce.dto.LoginResponseDTO;
import br.com.nathan.ecommerce.dto.RegisterDTO;
import br.com.nathan.ecommerce.mapper.RegisterMapper;
import br.com.nathan.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(AuthenticationDTO data) {
        UsernamePasswordAuthenticationToken usernamePassword =
                new UsernamePasswordAuthenticationToken(data.login(), data.password());

        Authentication auth = authenticationManager.authenticate(usernamePassword);
        String token = tokenService.generateToken((User) auth.getPrincipal());

        return new LoginResponseDTO(token);
    }

    public User register(RegisterDTO data) {
        if (Objects.nonNull(userRepository.findByLogin(data.login()))) {
            throw new RuntimeException("Usuário já existe!");
        }
        User user = RegisterMapper.toEntity(data);
        user.setPassword(passwordEncoder.encode(data.password()));
        return userRepository.save(user);
    }

}