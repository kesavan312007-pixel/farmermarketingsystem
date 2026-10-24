package com.farmersmarket.model;

/** Delivery details for a paid order. */
public class Delivery {
    private final String deliveryId;
    private final String orderId;
    private String deliveryAddress;
    private String contactPhone;
    private String scheduledDate;
    private String deliveryStatus = "SCHEDULED"; // SCHEDULED, IN_TRANSIT, DELIVERED
    private String trackingNotes = "Order packed and assigned to delivery agent.";

    public Delivery(String deliveryId, String orderId, String deliveryAddress,
                    String contactPhone, String scheduledDate) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveryAddress = deliveryAddress;
        this.contactPhone = contactPhone;
        this.scheduledDate = scheduledDate;
    }

    public String getDeliveryId() { return deliveryId; }
    public String getOrderId() { return orderId; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String a) { this.deliveryAddress = a; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String p) { this.contactPhone = p; }
    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String d) { this.scheduledDate = d; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String s) { this.deliveryStatus = s; }
    public String getTrackingNotes() { return trackingNotes; }
    public void setTrackingNotes(String n) { this.trackingNotes = n; }

    @Override
    public String toString() {
        return String.format("Delivery ID: %s | Order: %s | Status: %s | Date: %s | Address: %s | Phone: %s | Notes: %s",
                deliveryId, orderId, deliveryStatus, scheduledDate,
                deliveryAddress, contactPhone, trackingNotes);
    }
}
