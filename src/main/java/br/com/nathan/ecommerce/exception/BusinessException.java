package br.com.nathan.ecommerce.exception;

/**
 * Exceção base para erros de regras de negócio
 */
public abstract class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
