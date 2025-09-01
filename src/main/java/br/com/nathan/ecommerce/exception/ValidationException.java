package br.com.nathan.ecommerce.exception;

/**
 * Exceção para erros de validação de dados
 */
public class ValidationException extends BusinessException {
    
    private final String campo;
    private final Object valorInvalido;

    public ValidationException(String campo, Object valorInvalido, String motivo) {
        super(String.format("Erro de validação no campo '%s': %s. Valor: %s", campo, motivo, valorInvalido));
        this.campo = campo;
        this.valorInvalido = valorInvalido;
    }

    public ValidationException(String mensagem) {
        super(mensagem);
        this.campo = null;
        this.valorInvalido = null;
    }

    public String getCampo() {
        return campo;
    }

    public Object getValorInvalido() {
        return valorInvalido;
    }
}
