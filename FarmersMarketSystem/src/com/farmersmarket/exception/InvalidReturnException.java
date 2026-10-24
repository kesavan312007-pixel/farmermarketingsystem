package com.farmersmarket.exception;

/**
 * Thrown when a return violates policy or exceeds purchased quantity (Business Rule 4).
 */
public class InvalidReturnException extends MarketException {
    private static final long serialVersionUID = 1L;

    public InvalidReturnException(String message) {
        super(message);
    }
}
