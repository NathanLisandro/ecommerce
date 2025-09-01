package br.com.nathan.ecommerce.validator;

import br.com.nathan.ecommerce.base.BaseUnitTest;
import br.com.nathan.ecommerce.dto.ProdutoRequestDTO;
import br.com.nathan.ecommerce.fixtures.ProdutoFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProdutoValidatorTest extends BaseUnitTest {

    @InjectMocks
    private ProdutoValidator produtoValidator;

    private ProdutoRequestDTO produtoValido;

    @BeforeEach
    void setUp() {
        produtoValido = ProdutoFixture.createValidProdutoRequestDTO();
    }

    @Test
    @DisplayName("Deve validar produto válido com sucesso")
    void deveValidarProdutoValidoComSucesso() {
        assertDoesNotThrow(() -> produtoValidator.validarDadosProduto(produtoValido));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é nulo")
    void deveLancarExcecaoQuandoNomeNulo() {
        ProdutoRequestDTO produtoComNomeNulo = new ProdutoRequestDTO(
                null, produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComNomeNulo));

        assertTrue(exception.getMessage().contains("Nome do produto é obrigatório"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é muito curto")
    void deveLancarExcecaoQuandoNomeMuitoCurto() {
        ProdutoRequestDTO produtoComNomeCurto = new ProdutoRequestDTO(
                "A", produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComNomeCurto));

        assertTrue(exception.getMessage().contains("Nome do produto deve ter pelo menos 2 caracteres"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome é muito longo")
    void deveLancarExcecaoQuandoNomeMuitoLongo() {
        String nomeLongo = "A".repeat(256);
        ProdutoRequestDTO produtoComNomeLongo = new ProdutoRequestDTO(
                nomeLongo, produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComNomeLongo));

        assertTrue(exception.getMessage().contains("Nome do produto não pode exceder 255 caracteres"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando nome contém caracteres perigosos")
    void deveLancarExcecaoQuandoNomeContemCaracteresPerigosos() {
        ProdutoRequestDTO produtoComNomePerigoso = new ProdutoRequestDTO(
                "Produto<script>", produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComNomePerigoso));

        assertTrue(exception.getMessage().contains("Nome do produto contém caracteres não permitidos"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando descrição é muito longa")
    void deveLancarExcecaoQuandoDescricaoMuitoLonga() {
        String descricaoLonga = "A".repeat(1001);
        ProdutoRequestDTO produtoComDescricaoLonga = new ProdutoRequestDTO(
                produtoValido.nome(), descricaoLonga, produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComDescricaoLonga));

        assertTrue(exception.getMessage().contains("Descrição do produto não pode exceder 1000 caracteres"));
    }

    @Test
    @DisplayName("Deve validar descrição nula")
    void deveValidarDescricaoNula() {
        ProdutoRequestDTO produtoComDescricaoNula = new ProdutoRequestDTO(
                produtoValido.nome(), null, produtoValido.preco(), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        assertDoesNotThrow(() -> produtoValidator.validarDadosProduto(produtoComDescricaoNula));
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é nulo")
    void deveLancarExcecaoQuandoPrecoNulo() {
        ProdutoRequestDTO produtoComPrecoNulo = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), null, 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComPrecoNulo));

        assertTrue(exception.getMessage().contains("Preço do produto é obrigatório"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço é zero ou negativo")
    void deveLancarExcecaoQuandoPrecoZeroOuNegativo() {
        ProdutoRequestDTO produtoComPrecoZero = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), BigDecimal.ZERO, 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        ProdutoRequestDTO produtoComPrecoNegativo = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), new BigDecimal("-10.00"), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComPrecoZero));

        IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComPrecoNegativo));

        assertTrue(exception1.getMessage().contains("Preço do produto deve ser maior que zero"));
        assertTrue(exception2.getMessage().contains("Preço do produto deve ser maior que zero"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço excede limite máximo")
    void deveLancarExcecaoQuandoPrecoExcedeLimiteMaximo() {
        ProdutoRequestDTO produtoComPrecoAlto = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), new BigDecimal("2000000.00"), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComPrecoAlto));

        assertTrue(exception.getMessage().contains("Preço do produto não pode exceder R$ 1.000.000,00"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando preço tem muitas casas decimais")
    void deveLancarExcecaoQuandoPrecoTemMuitasCasasDecimais() {
        ProdutoRequestDTO produtoComPrecoDecimais = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), new BigDecimal("10.999"), 
                produtoValido.categoria(), produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComPrecoDecimais));

        assertTrue(exception.getMessage().contains("Preço do produto deve ter no máximo 2 casas decimais"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria é nula")
    void deveLancarExcecaoQuandoCategoriaNula() {
        ProdutoRequestDTO produtoComCategoriaNula = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), produtoValido.preco(), 
                null, produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComCategoriaNula));

        assertTrue(exception.getMessage().contains("Categoria do produto é obrigatória"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando categoria tem caracteres inválidos")
    void deveLancarExcecaoQuandoCategoriaTemCaracteresInvalidos() {
        ProdutoRequestDTO produtoComCategoriaInvalida = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), produtoValido.preco(), 
                "Categoria@Inválida!", produtoValido.quantidadeEstoque());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComCategoriaInvalida));

        assertTrue(exception.getMessage().contains("Categoria contém caracteres não permitidos"));
    }

    @Test
    @DisplayName("Deve validar categoria com caracteres válidos")
    void deveValidarCategoriaComCaracteresValidos() {
        ProdutoRequestDTO produtoComCategoriaValida = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), produtoValido.preco(), 
                "Eletrônicos-Smartphones", produtoValido.quantidadeEstoque());

        assertDoesNotThrow(() -> produtoValidator.validarDadosProduto(produtoComCategoriaValida));
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade de estoque é negativa")
    void deveLancarExcecaoQuandoQuantidadeEstoqueNegativa() {
        ProdutoRequestDTO produtoComEstoqueNegativo = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), -1L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComEstoqueNegativo));

        assertTrue(exception.getMessage().contains("Quantidade em estoque não pode ser negativa"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando quantidade de estoque excede limite")
    void deveLancarExcecaoQuandoQuantidadeEstoqueExcedeLimite() {
        ProdutoRequestDTO produtoComEstoqueExcessivo = new ProdutoRequestDTO(
                produtoValido.nome(), produtoValido.descricao(), produtoValido.preco(), 
                produtoValido.categoria(), 2_000_000L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarDadosProduto(produtoComEstoqueExcessivo));

        assertTrue(exception.getMessage().contains("Quantidade em estoque não pode exceder 1.000.000 unidades"));
    }

    @Test
    @DisplayName("Deve validar parâmetros de busca válidos")
    void deveValidarParametrosDeBuscaValidos() {
        assertDoesNotThrow(() -> produtoValidator.validarParametrosBusca("Smartphone", "Eletrônicos"));
        assertDoesNotThrow(() -> produtoValidator.validarParametrosBusca("Smartphone", null));
        assertDoesNotThrow(() -> produtoValidator.validarParametrosBusca(null, "Eletrônicos"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando termo de busca é muito curto")
    void deveLancarExcecaoQuandoTermoDeBuscaMuitoCurto() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarParametrosBusca("A", null));

        assertTrue(exception.getMessage().contains("Nome para busca deve ter pelo menos 2 caracteres"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando termo de busca contém caracteres perigosos")
    void deveLancarExcecaoQuandoTermoDeBuscaContemCaracteresPerigosos() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarParametrosBusca("produto'; DROP TABLE", null));

        assertTrue(exception.getMessage().contains("Nome contém caracteres não permitidos"));
    }

    @Test
    @DisplayName("Deve validar atualização de estoque")
    void deveValidarAtualizacaoDeEstoque() {
        assertDoesNotThrow(() -> produtoValidator.validarAtualizacaoEstoque(100L));
        assertDoesNotThrow(() -> produtoValidator.validarAtualizacaoEstoque(0L));
    }

    @Test
    @DisplayName("Deve lançar exceção quando atualização de estoque é nula")
    void deveLancarExcecaoQuandoAtualizacaoDeEstoqueNula() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarAtualizacaoEstoque(null));

        assertTrue(exception.getMessage().contains("Quantidade de estoque é obrigatória"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando atualização de estoque é negativa")
    void deveLancarExcecaoQuandoAtualizacaoDeEstoqueNegativa() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> produtoValidator.validarAtualizacaoEstoque(-5L));

        assertTrue(exception.getMessage().contains("Quantidade de estoque não pode ser negativa"));
    }
}
