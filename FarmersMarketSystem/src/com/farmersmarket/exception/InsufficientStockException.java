package com.farmersmarket.exception;

/**
 * Thrown when an order exceeds the vendor's available inventory (Business Rule 1).
 */
public class InsufficientStockException extends MarketException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
