package com.farmersmarket.model;

/**
 * One line inside an Order. Rule 5: links directly to a vendor's Product.
 * The unit price is captured at purchase time so later price changes
 * do not alter old invoices.
 */
public class OrderItem {
    private String orderItemId;
    private Product product;
    private int quantity;
    private double unitPrice;

    public OrderItem(String orderItemId, Product product, int quantity) {
        this.orderItemId = orderItemId;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getUnitPrice();
    }

    public String getOrderItemId() { return orderItemId; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return quantity * unitPrice; }

    @Override
    public String toString() {
        return String.format("%-9s %-28s %-30s %4d %10.2f %12.2f",
                product.getProductId(), product.getName(),
                product.getVendor().getName(), quantity, unitPrice, getSubtotal());
    }
}
