package com.farmersmarket.exception;

/**
 * Thrown when input fails format or boundary validation.
 */
public class InvalidInputException extends MarketException {
    private static final long serialVersionUID = 1L;

    public InvalidInputException(String message) {
        super(message);
    }
}
