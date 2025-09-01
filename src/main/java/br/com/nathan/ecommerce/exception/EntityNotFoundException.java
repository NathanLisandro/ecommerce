package br.com.nathan.ecommerce.exception;

import java.util.UUID;

/**
 * Exceção lançada quando uma entidade não é encontrada
 */
public class EntityNotFoundException extends BusinessException {
    
    private final String entityType;
    private final UUID entityId;

    public EntityNotFoundException(String entityType, UUID entityId) {
        super(String.format("%s não encontrado com ID: %s", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public EntityNotFoundException(String entityType, String identifier) {
        super(String.format("%s não encontrado: %s", entityType, identifier));
        this.entityType = entityType;
        this.entityId = null;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }
}
