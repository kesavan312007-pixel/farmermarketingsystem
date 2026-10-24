package com.farmersmarket.model;

/** Card payment: only the last four digits are ever stored or shown. */
public class CardPayment extends Payment {
    private final String cardNumberMasked;
    private final String cardHolderName;
    private final String transactionRef;

    public CardPayment(String paymentId, String orderId, double amount, String paymentDate,
                       String cardNumber, String cardHolderName, String transactionRef) {
        super(paymentId, orderId, amount, paymentDate);
        this.cardHolderName = cardHolderName;
        this.transactionRef = transactionRef;
        this.cardNumberMasked = "XXXX-XXXX-XXXX-" + cardNumber.substring(cardNumber.length() - 4);
    }

    public String getCardNumberMasked() { return cardNumberMasked; }
    public String getCardHolderName() { return cardHolderName; }
    public String getTransactionRef() { return transactionRef; }

    @Override
    public boolean processPayment() {
        setPaymentStatus(SETTLED);
        return true;
    }

    @Override
    public String getPaymentMode() { return "CARD"; }

    @Override
    public String toString() {
        return super.toString() + String.format(" | Card: %s | Holder: %s | Txn: %s",
                cardNumberMasked, cardHolderName, transactionRef);
    }
}
