package br.com.nathan.ecommerce.mapper;

import br.com.nathan.ecommerce.domain.User;
import br.com.nathan.ecommerce.dto.RegisterDTO;

public class RegisterMapper {

    public static User toEntity(RegisterDTO registerDTO){
        return User.builder()
                .role(registerDTO.role())
                .login(registerDTO.login())
                .password(registerDTO.password())
                .build();
    }

}
