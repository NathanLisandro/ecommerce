package br.com.nathan.ecommerce.controller;

import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.dto.AuthenticationDTO;
import br.com.nathan.ecommerce.dto.LoginResponseDTO;
import br.com.nathan.ecommerce.dto.RegisterDTO;
import br.com.nathan.ecommerce.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody AuthenticationDTO data) {
        LoginResponseDTO response = authenticationService.login(data);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterDTO data) {
        User user = authenticationService.register(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}