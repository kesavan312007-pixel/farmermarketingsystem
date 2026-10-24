package com.farmersmarket.model;

import com.farmersmarket.exception.InsufficientStockException;

/**
 * A farm product owned by exactly one Vendor.
 * Rule 2: stock decreases on order confirmation and increases on approved returns.
 * Rule 5: the product links directly to its vendor.
 */
public class Product {
    private String productId;
    private String name;
    private String category;
    private double unitPrice;
    private int availableStock;
    private Vendor vendor; // Rule 5

    public Product(String productId, String name, String category,
                   double unitPrice, int availableStock, Vendor vendor) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.availableStock = availableStock;
        this.vendor = vendor;
        if (vendor != null) {
            vendor.addProduct(this);
        }
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public int getAvailableStock() { return availableStock; }
    public Vendor getVendor() { return vendor; }

    /** Rule 2: reduce stock when an order is confirmed. */
    public void decreaseStock(int quantity) throws InsufficientStockException {
        if (quantity > availableStock) {
            throw new InsufficientStockException("Insufficient stock for " + name
                    + " (ID: " + productId + "). Requested: " + quantity
                    + ", Available: " + availableStock
                    + " from vendor " + vendor.getName());
        }
        availableStock -= quantity;
    }

    /** Rule 2: add stock back when a return is approved. */
    public void increaseStock(int quantity) {
        availableStock += quantity;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-28s | %-10s | Rs.%8.2f | Stock: %3d | Vendor: %s",
                productId, name, category, unitPrice, availableStock,
                vendor != null ? vendor.getName() : "N/A");
    }
}
