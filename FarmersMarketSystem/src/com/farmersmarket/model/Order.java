package com.farmersmarket.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A customer order.
 * Composition: an Order owns its OrderItems.
 * Aggregation: an Order is associated with a Payment, a Delivery and ReturnRequests.
 */
public class Order {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final String orderId;
    private final String customerName;
    private final String customerContact;
    private final LocalDate orderDate;
    private final List<OrderItem> items = new ArrayList<>();           // Composition
    private final List<ReturnRequest> returnRequests = new ArrayList<>();
    private Payment payment;                                            // Aggregation (polymorphic)
    private Delivery delivery;                                          // Aggregation
    private String orderStatus = "CONFIRMED";

    public Order(String orderId, String customerName, String customerContact, LocalDate orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerContact = customerContact;
        this.orderDate = orderDate;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerContact() { return customerContact; }
    public LocalDate getOrderLocalDate() { return orderDate; }
    public String getOrderDate() { return orderDate.format(FMT); }
    public List<OrderItem> getItems() { return Collections.unmodifiableList(items); }
    public List<ReturnRequest> getReturnRequests() { return Collections.unmodifiableList(returnRequests); }
    public Payment getPayment() { return payment; }
    public Delivery getDelivery() { return delivery; }
    public String getOrderStatus() { return orderStatus; }

    public void addItem(OrderItem item) { items.add(item); }
    public void setPayment(Payment payment) { this.payment = payment; orderStatus = "PAID"; }
    public void setDelivery(Delivery delivery) { this.delivery = delivery; orderStatus = "DELIVERY_SCHEDULED"; }
    public void addReturnRequest(ReturnRequest r) { returnRequests.add(r); }

    public double calculateTotal() {
        double total = 0.0;
        for (OrderItem item : items) total += item.getSubtotal();
        return total;
    }

    /** Sum of refunds for approved returns. */
    public double getRefundedAmount() {
        double sum = 0.0;
        for (ReturnRequest r : returnRequests)
            if (ReturnRequest.APPROVED.equals(r.getStatus())) sum += r.getRefundAmount();
        return sum;
    }

    public double getNetTotal() { return calculateTotal() - getRefundedAmount(); }

    public boolean isPaymentSettled() {
        return payment != null && Payment.SETTLED.equalsIgnoreCase(payment.getPaymentStatus());
    }

    public OrderItem findItemByProductId(String productId) {
        for (OrderItem item : items)
            if (item.getProduct().getProductId().equalsIgnoreCase(productId)) return item;
        return null;
    }

    /** Quantity of a product already returned through approved requests. */
    public int getReturnedQuantityForProduct(String productId) {
        int sum = 0;
        for (ReturnRequest r : returnRequests)
            if (r.getProductId().equalsIgnoreCase(productId)
                    && ReturnRequest.APPROVED.equals(r.getStatus()))
                sum += r.getRequestedQuantity();
        return sum;
    }
}
