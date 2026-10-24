package com.farmersmarket.model;

/** A customer's request to return part of an order line (Rule 4). */
public class ReturnRequest {
    public static final String APPROVED = "APPROVED";
    public static final String REJECTED = "REJECTED";

    private final String returnId;
    private final String orderId;
    private final String productId;
    private final int requestedQuantity;
    private final String reason;
    private final String requestDate;
    private String status = "PENDING";
    private String decisionRemarks = "Awaiting verification";
    private double refundAmount;

    public ReturnRequest(String returnId, String orderId, String productId,
                         int requestedQuantity, String reason, String requestDate) {
        this.returnId = returnId;
        this.orderId = orderId;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.reason = reason;
        this.requestDate = requestDate;
    }

    public String getReturnId() { return returnId; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public String getReason() { return reason; }
    public String getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public String getDecisionRemarks() { return decisionRemarks; }
    public double getRefundAmount() { return refundAmount; }

    /** Records the final decision of the return policy check. */
    public void decide(boolean approved, String remarks, double refund) {
        this.status = approved ? APPROVED : REJECTED;
        this.decisionRemarks = remarks;
        this.refundAmount = approved ? refund : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Return ID: %s | Order: %s | Product: %s | Qty: %d | Refund: Rs.%.2f | Status: %s | Remarks: %s",
                returnId, orderId, productId, requestedQuantity, refundAmount, status, decisionRemarks);
    }
}
