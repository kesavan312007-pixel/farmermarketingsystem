package com.farmersmarket.model;

/** Cash payment: settles only if the tendered amount covers the bill. */
public class CashPayment extends Payment {
    private final double tenderedAmount;

    public CashPayment(String paymentId, String orderId, double amount,
                       String paymentDate, double tenderedAmount) {
        super(paymentId, orderId, amount, paymentDate);
        this.tenderedAmount = tenderedAmount;
    }

    public double getTenderedAmount() { return tenderedAmount; }
    public double getChangeReturned() { return Math.max(0.0, tenderedAmount - getAmount()); }

    @Override
    public boolean processPayment() {
        boolean ok = tenderedAmount >= getAmount();
        setPaymentStatus(ok ? SETTLED : FAILED);
        return ok;
    }

    @Override
    public String getPaymentMode() { return "CASH"; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Tendered: Rs.%.2f | Change: Rs.%.2f",
                tenderedAmount, getChangeReturned());
    }
}
