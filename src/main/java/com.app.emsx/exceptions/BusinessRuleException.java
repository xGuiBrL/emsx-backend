package com.app.emsx.exceptions;

/**
 * ⚖️ BusinessRuleException
 * ---------------------------------------------------------
 * Excepción personalizada para violaciones de reglas de negocio.
 *
 * Se lanza cuando una acción o dato incumple una regla lógica de la aplicación,
 * por ejemplo:
 *   - Crear una orden sin stock suficiente
 *   - Crear un shipment para una orden que ya tiene uno
 *   - Intentar crear un producto con SKU duplicado
 *
 * Es capturada por el GlobalExceptionHandler y devuelve HTTP 409 (Conflict)
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
