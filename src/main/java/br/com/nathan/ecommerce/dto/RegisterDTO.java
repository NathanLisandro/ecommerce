package br.com.nathan.ecommerce.dto;

import br.com.nathan.ecommerce.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RegisterDTO(
        @NotBlank(message = "Login é obrigatório")
        String login,
        
        @NotBlank(message = "Password é obrigatório")
        String password,
        
        @NotNull(message = "Role é obrigatório")
        UserRole role
) {
}