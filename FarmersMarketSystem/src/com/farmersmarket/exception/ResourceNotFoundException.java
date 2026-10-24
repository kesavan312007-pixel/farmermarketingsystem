package com.farmersmarket.exception;

/**
 * Thrown when a Vendor, Product or Order cannot be found.
 */
public class ResourceNotFoundException extends MarketException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
