package com.farmersmarket.model;

/**
 * Abstract payment. Demonstrates Inheritance and Polymorphism:
 * CashPayment, CardPayment and UPIPayment each implement processPayment()
 * and getPaymentMode() differently.
 */
public abstract class Payment {
    public static final String PENDING = "PENDING";
    public static final String SETTLED = "SETTLED";
    public static final String FAILED = "FAILED";

    private final String paymentId;
    private final String orderId;
    private final double amount;
    private final String paymentDate;
    private String paymentStatus = PENDING;

    protected Payment(String paymentId, String orderId, double amount, String paymentDate) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentDate = paymentDate;
    }

    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public double getAmount() { return amount; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentStatus() { return paymentStatus; }
    protected void setPaymentStatus(String status) { this.paymentStatus = status; }

    /** Attempts the payment; returns true when settled. */
    public abstract boolean processPayment();

    public abstract String getPaymentMode();

    @Override
    public String toString() {
        return String.format("Payment ID: %s | Order: %s | Mode: %s | Amount: Rs.%.2f | Status: %s | Date: %s",
                paymentId, orderId, getPaymentMode(), amount, paymentStatus, paymentDate);
    }
}
