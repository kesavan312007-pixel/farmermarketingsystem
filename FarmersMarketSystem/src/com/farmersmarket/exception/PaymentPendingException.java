package com.farmersmarket.exception;

/**
 * Thrown when delivery is scheduled before payment is settled (Business Rule 3).
 */
public class PaymentPendingException extends MarketException {
    private static final long serialVersionUID = 1L;

    public PaymentPendingException(String message) {
        super(message);
    }
}
