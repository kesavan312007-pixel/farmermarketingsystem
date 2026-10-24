package com.farmersmarket.exception;

/**
 * Base custom checked exception for the Farmers' Market domain.
 */
public class MarketException extends Exception {
    private static final long serialVersionUID = 1L;

    public MarketException(String message) {
        super(message);
    }
}
