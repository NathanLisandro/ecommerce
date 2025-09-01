package br.com.nathan.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationDTO(
        @NotBlank(message = "Login é obrigatório")
        String login,
        
        @NotBlank(message = "Password é obrigatório")
        String password
) {
}