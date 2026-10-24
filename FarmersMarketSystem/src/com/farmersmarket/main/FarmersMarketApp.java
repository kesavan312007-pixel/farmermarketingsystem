package com.farmersmarket.main;

import com.farmersmarket.exception.*;
import com.farmersmarket.model.*;
import com.farmersmarket.service.FarmersMarketService;
import com.farmersmarket.util.ValidationUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Menu-driven console interface for the Farmers' Market Orders & Payments system.
 */
public class FarmersMarketApp {

    private static final Scanner scanner = new Scanner(System.in);
    private static final FarmersMarketService service = new FarmersMarketService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("        FARMERS' MARKET ORDERS & PAYMENTS MANAGEMENT SYSTEM");
        System.out.println("=".repeat(80));

        boolean exit = false;
        while (!exit) {
            try {
                displayMenu();
                int choice = ValidationUtil.readMenuChoice(scanner, 1, 9);
                switch (choice) {
                    case 1: handleAddVendor(); break;
                    case 2: handleAddProduct(); break;
                    case 3: handlePlaceOrder(); break;
                    case 4: handleRecordPayment(); break;
                    case 5: handleScheduleDelivery(); break;
                    case 6: handleRequestReturn(); break;
                    case 7: service.displayInventory(); break;
                    case 8: service.displayCustomerOrderHistory(); break;
                    default:
                        System.out.println("\nThank you for using the Farmers' Market System. Goodbye!");
                        exit = true;
                }
            } catch (MarketException e) {
                System.out.println("\n[BUSINESS RULE VIOLATION]: " + e.getMessage());
            } catch (NoSuchElementException e) {
                System.out.println("\nInput closed. Exiting.");
                exit = true;
            } catch (Exception e) {
                System.out.println("\n[SYSTEM ERROR]: " + e.getMessage());
            }
        }
    }

    private static void displayMenu() {
        String bar = "+" + "-".repeat(50) + "+";
        System.out.println("\n" + bar);
        System.out.println("|              FARMERS' MARKET MAIN MENU           |");
        System.out.println(bar);
        String[] items = {"Add Vendor", "Add Product", "Place Order", "Record Payment",
                "Schedule Delivery", "Request Return", "Display Inventory",
                "Customer Order History & Totals", "Exit"};
        for (int i = 0; i < items.length; i++)
            System.out.printf("| %d. %-45s |%n", i + 1, items[i]);
        System.out.println(bar);
    }

    private static void handleAddVendor() {
        System.out.println("\n--- [1] ADD VENDOR ---");
        String name = ValidationUtil.readNonEmptyString(scanner, "Enter Vendor / Farm Name : ");
        String loc = ValidationUtil.readNonEmptyString(scanner, "Enter Farm Location      : ");
        String ph = ValidationUtil.readPhoneNumber(scanner, "Enter Contact Phone (10 digits): ");
        Vendor v = service.addVendor(name, loc, ph);
        System.out.println(">> Success! Vendor registered: " + v);
    }

    private static void handleAddProduct() throws ResourceNotFoundException {
        System.out.println("\n--- [2] ADD PRODUCT ---");
        System.out.println("Available Vendors:");
        for (Vendor v : service.getVendors().values()) System.out.println("  " + v);
        String vid = ValidationUtil.readNonEmptyString(scanner, "Enter Vendor ID: ");
        String name = ValidationUtil.readNonEmptyString(scanner, "Enter Product Name: ");
        String cat = ValidationUtil.readNonEmptyString(scanner, "Enter Category (e.g. Fruits, Dairy): ");
        double price = ValidationUtil.readPositiveDouble(scanner, "Enter Unit Price (Rs.): ");
        int stock = ValidationUtil.readPositiveInt(scanner, "Enter Initial Stock: ");
        Product p = service.addProduct(name, cat, price, stock, vid);
        System.out.println(">> Product added: " + p);
        service.displayVendorStockUpdates("PRODUCT ADDED (" + p.getProductId() + ")",
                java.util.Collections.singleton(p.getVendor()));
    }

    private static void handlePlaceOrder()
            throws ResourceNotFoundException, InsufficientStockException, InvalidInputException {
        System.out.println("\n--- [3] PLACE ORDER ---");
        System.out.println("Available Catalog:");
        for (Product p : service.getProducts().values()) System.out.println("  " + p);

        String name = ValidationUtil.readNonEmptyString(scanner, "Enter Customer Name: ");
        String phone = ValidationUtil.readPhoneNumber(scanner, "Enter Customer Phone (10 digits): ");

        Map<String, Integer> cart = new LinkedHashMap<>();
        boolean adding = true;
        while (adding) {
            String pid = ValidationUtil.readNonEmptyString(scanner, "Enter Product ID: ").toUpperCase();
            try {
                service.getProduct(pid); // reject unknown IDs immediately
                int qty = ValidationUtil.readPositiveInt(scanner, "Enter Quantity: ");
                cart.merge(pid, qty, Integer::sum);
            } catch (ResourceNotFoundException e) {
                System.out.println(">> Error: " + e.getMessage());
            }
            if (cart.isEmpty()) continue;
            adding = ValidationUtil.readYesNo(scanner, "Add another product? (yes/no): ");
        }
        Order o = service.placeOrder(name, phone, cart);
        System.out.println(">> Order placed successfully! Order ID: " + o.getOrderId());
    }

    private static void handleRecordPayment() throws ResourceNotFoundException, MarketException {
        System.out.println("\n--- [4] RECORD PAYMENT ---");
        String oid = ValidationUtil.readNonEmptyString(scanner, "Enter Order ID: ");
        Order order = service.findOrder(oid);
        if (order.isPaymentSettled()) {
            System.out.println(">> Payment already settled for " + order.getOrderId());
            return;
        }
        double due = order.calculateTotal();
        System.out.printf("Total payable for %s: Rs.%.2f%n", order.getOrderId(), due);
        System.out.println("Select Payment Method:  1. Cash   2. Card   3. UPI");
        int m = ValidationUtil.readMenuChoice(scanner, 1, 3);

        String pid = service.generateNextPaymentId();
        String today = LocalDate.now().format(FMT);
        Payment pay;
        if (m == 1) {
            double cash = ValidationUtil.readPositiveDouble(scanner, "Enter Cash Tendered (Rs.): ");
            pay = new CashPayment(pid, order.getOrderId(), due, today, cash);
        } else if (m == 2) {
            String card = ValidationUtil.readCardNumber(scanner, "Enter 16-digit Card Number: ");
            String holder = ValidationUtil.readNonEmptyString(scanner, "Enter Cardholder Name: ");
            pay = new CardPayment(pid, order.getOrderId(), due, today, card, holder,
                    "TXN-" + System.currentTimeMillis());
        } else {
            String upi = ValidationUtil.readUpiId(scanner, "Enter UPI ID (e.g. name@oksbi): ");
            pay = new UPIPayment(pid, order.getOrderId(), due, today, upi,
                    "UPI-" + System.currentTimeMillis());
        }
        Payment result = service.recordPayment(order.getOrderId(), pay); // polymorphic call
        System.out.println("\n>> Payment Status: " + result.getPaymentStatus());
        System.out.println(">> Receipt: " + result);
    }

    private static void handleScheduleDelivery()
            throws ResourceNotFoundException, PaymentPendingException, InvalidInputException {
        System.out.println("\n--- [5] SCHEDULE DELIVERY ---");
        String oid = ValidationUtil.readNonEmptyString(scanner, "Enter Order ID: ");
        Order o = service.findOrder(oid);
        if (!o.isPaymentSettled()) { // Rule 3 is also enforced inside the service
            throw new PaymentPendingException("Payment Pending! Order " + o.getOrderId()
                    + " is not settled. Settle payment before scheduling delivery.");
        }
        String addr = ValidationUtil.readNonEmptyString(scanner, "Enter Delivery Address: ");
        String ph = ValidationUtil.readPhoneNumber(scanner, "Enter Recipient Phone: ");
        String date = LocalDate.now().plusDays(1).format(FMT);
        Delivery d = service.scheduleDelivery(o.getOrderId(), addr, ph, date);
        System.out.println("\n>> Delivery scheduled successfully!");
        System.out.println(">> " + d);
    }

    private static void handleRequestReturn() throws ResourceNotFoundException, InvalidReturnException {
        System.out.println("\n--- [6] REQUEST RETURN ---");
        String oid = ValidationUtil.readNonEmptyString(scanner, "Enter Order ID: ");
        Order o = service.findOrder(oid);
        System.out.println("Items in this order:");
        for (OrderItem it : o.getItems())
            System.out.printf("  - %s | %-28s | Purchased: %d | Returned: %d%n",
                    it.getProduct().getProductId(), it.getProduct().getName(), it.getQuantity(),
                    o.getReturnedQuantityForProduct(it.getProduct().getProductId()));
        String pid = ValidationUtil.readNonEmptyString(scanner, "Enter Product ID to return: ");
        int qty = ValidationUtil.readPositiveInt(scanner, "Enter Return Quantity: ");
        String reason = ValidationUtil.readNonEmptyString(scanner, "Enter Reason for Return: ");

        ReturnRequest r = service.requestReturn(o.getOrderId(), pid, qty, reason);
        System.out.println("\n>> Return processed!");
        System.out.println(">> Return ID : " + r.getReturnId());
        System.out.println(">> Decision  : " + r.getStatus());
        System.out.println(">> Remarks   : " + r.getDecisionRemarks());
        if (ReturnRequest.APPROVED.equals(r.getStatus())) {
            System.out.printf(">> Refund    : Rs.%.2f%n", r.getRefundAmount());
            System.out.println(">> Vendor stock updated.");
        }
    }
}
