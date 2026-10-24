package com.farmersmarket.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A local farmer/producer who sells products.
 * Demonstrates Encapsulation and Aggregation (a Vendor has Products).
 */
public class Vendor {
    private String vendorId;
    private String name;
    private String farmLocation;
    private String contactNumber;
    private final List<Product> products = new ArrayList<>(); // Aggregation

    public Vendor(String vendorId, String name, String farmLocation, String contactNumber) {
        this.vendorId = vendorId;
        this.name = name;
        this.farmLocation = farmLocation;
        this.contactNumber = contactNumber;
    }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFarmLocation() { return farmLocation; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    /** Read-only view so outside code cannot modify the list directly. */
    public List<Product> getProducts() { return Collections.unmodifiableList(products); }

    public void addProduct(Product product) { products.add(product); }

    @Override
    public String toString() {
        return String.format("[%s] %s (Location: %s, Phone: %s)",
                vendorId, name, farmLocation, contactNumber);
    }
}
