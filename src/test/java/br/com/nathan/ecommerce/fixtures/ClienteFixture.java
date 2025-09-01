package br.com.nathan.ecommerce.fixtures;

import br.com.nathan.ecommerce.domain.Cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ClienteFixture {

    public static Cliente createValidCliente() {
        return createClienteWithParams("João Silva", "joao@email.com", "12345678901");
    }

    public static Cliente createClienteWithId(UUID id) {
        return Cliente.builder()
                .id(id)
                .nome("João Silva")
                .email("joao@email.com")
                .telefone("11999999999")
                .cpf("12345678901")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Cliente createClienteWithParams(String nome, String email, String cpf) {
        return Cliente.builder()
                .nome(nome)
                .email(email)
                .telefone("11999999999")
                .cpf(cpf)
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Cliente createClienteWithAllParams(UUID id, String nome, String email, String cpf, LocalDate dataNascimento) {
        return Cliente.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone("11999999999")
                .cpf(cpf)
                .dataNascimento(dataNascimento)
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Cliente createClienteWithEmail(String email) {
        return Cliente.builder()
                .nome("Cliente Teste")
                .email(email)
                .telefone("11999999999")
                .cpf("12345678901")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }

    public static Cliente createClienteWithNome(String nome) {
        return Cliente.builder()
                .nome(nome)
                .email("cliente@email.com")
                .telefone("11999999999")
                .cpf("12345678901")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .dataCadastro(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();
    }
}